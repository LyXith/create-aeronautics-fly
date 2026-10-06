package foundry.veil.api.client.render.framebuffer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * An off-screen render target.
 *
 * <p>Veil 1.21.1 exposed this as a raw GL framebuffer object you {@code bind()}ed and
 * {@code clear()}ed. 26.3 has no notion of a "currently bound framebuffer" at all:
 * work is recorded into a {@link RenderPass} that names its attachments up front.
 * The old {@code bind/clear/unbind} triple therefore collapses into {@link #begin(boolean)}
 * plus {@link Pass#draw()}, which prepares the submitted models and executes them
 * straight into this target.
 */
public final class AdvancedFbo {
    private static final Vector4f CLEAR_COLOR = new Vector4f(0f, 0f, 0f, 0f);

    private final String label;
    private final RenderTarget target;
    private final boolean depthBuffered;

    private AdvancedFbo(final String label, final int width, final int height, final boolean depthBuffered) {
        this.label = label;
        this.depthBuffered = depthBuffered;
        this.target = new TextureTarget(label, width, height, GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
    }

    public static Builder withSize(final int width, final int height) {
        return new Builder(width, height);
    }

    /**
     * Starts a render pass against this target. Geometry is recorded by submitting into
     * {@link Pass#collector()}; {@link Pass#draw()} performs the GPU work.
     */
    public Pass begin(final boolean clear) {
        return new Pass(this, clear);
    }

    public int width() {
        return this.target.width;
    }

    public int height() {
        return this.target.height;
    }

    public String label() {
        return this.label;
    }

    public RenderTarget target() {
        return this.target;
    }

    public boolean hasDepth() {
        return this.depthBuffered && this.target.hasDepth();
    }

    public GpuTextureView colorView() {
        return this.target.getColorTextureView();
    }

    public GpuTextureView depthView() {
        return this.hasDepth() ? this.target.getDepthTextureView() : null;
    }

    public Attachment getColorTextureAttachment(final int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("AdvancedFbo only exposes a single colour attachment: " + index);
        }
        return new Attachment(this.target.getColorTextureView());
    }

    public Attachment getDepthTextureAttachment() {
        if (!this.hasDepth()) {
            throw new IllegalStateException(this.label + " has no depth attachment");
        }
        return new Attachment(this.target.getDepthTextureView());
    }

    /** Frees the GPU allocations. The instance must not be used afterwards. */
    public void free() {
        this.target.destroyBuffers();
    }

    /**
     * Reads a rectangular region of the colour attachment back to the CPU.
     *
     * <p>26.3 removed immediate {@code glReadPixels}; a texture-to-buffer copy plus a
     * fence is the supported readback path (the same one {@code Screenshot} uses, only
     * awaited here because the caller needs the pixels right now).
     *
     * @return {@code true} when the copy completed and {@code out} was filled
     */
    public boolean readPixels(final int x, final int y, final int width, final int height, final int[] out) {
        RenderSystem.assertOnRenderThread();

        final int byteCount = width * height * 4;
        if (out.length < width * height || byteCount <= 0) {
            return false;
        }

        final GpuDevice device = RenderSystem.getDevice();
        final CommandEncoder encoder = device.createCommandEncoder();
        final GpuBuffer buffer = device.createBuffer(
                () -> this.label + "-readback", GpuBuffer.USAGE_MAP_READ, byteCount);

        try {
            encoder.copyTextureToBuffer(this.target.getColorTexture(), buffer, 0L, () -> {
            }, x, y, width, height, 0);
            final GpuFence fence = encoder.createFence();
            final boolean finished = fence.awaitCompletion(java.util.concurrent.TimeUnit.SECONDS.toNanos(5));
            fence.close();

            if (!finished) {
                return false;
            }

            try (GpuBufferSlice.MappedView view = buffer.map(true, false)) {
                final ByteBuffer data = view.data();
                final int pixels = width * height;
                for (int i = 0; i < pixels; i++) {
                    out[i] = data.getInt(i * 4);
                }
                return true;
            }
        } finally {
            buffer.close();
        }
    }

    /** The colour/depth attachment of an {@link AdvancedFbo}. */
    public record Attachment(GpuTextureView view) {
        public GpuTextureView getTextureView() {
            return this.view;
        }
    }

    public static final class Builder {
        private final int width;
        private final int height;
        private boolean color;
        private boolean depth;

        private Builder(final int width, final int height) {
            this.width = width;
            this.height = height;
        }

        public Builder addColorTextureBuffer() {
            this.color = true;
            return this;
        }

        public Builder setDepthTextureBuffer() {
            this.depth = true;
            return this;
        }

        public AdvancedFbo build(final boolean resize) {
            if (!this.color) {
                throw new IllegalStateException("AdvancedFbo requires a colour attachment");
            }
            return new AdvancedFbo("veil-fbo-" + this.width + "x" + this.height, this.width, this.height, this.depth);
        }
    }

    /**
     * A single off-screen render pass. Submit models through {@link #collector()}, then
     * call {@link #draw()} (or simply {@link #close()}, which draws if you have not).
     */
    public static final class Pass implements AutoCloseable {
        private final AdvancedFbo owner;
        private final boolean clear;
        private final SubmitNodeStorage storage = new SubmitNodeStorage();

        private boolean drawn;

        private Pass(final AdvancedFbo owner, final boolean clear) {
            this.owner = owner;
            this.clear = clear;
        }

        /** The collector that models submitted during this pass are recorded into. */
        public SubmitNodeStorage collector() {
            return this.storage;
        }

        /** Prepares everything recorded so far and renders it into the target. */
        public void draw() {
            if (this.drawn) {
                return;
            }
            this.drawn = true;

            final FeatureRenderDispatcher dispatcher = Minecraft.getInstance().gameRenderer.featureRenderDispatcher();
            final FeatureRenderDispatcher.PreparedFrame prepared = dispatcher.prepareFrame(this.storage);

            final RenderPassDescriptor.Builder descriptor = RenderPassDescriptor.builder(() -> this.owner.label)
                    .withColorAttachment(this.owner.colorView(),
                            this.clear ? Optional.of(CLEAR_COLOR) : Optional.empty());
            if (this.owner.hasDepth()) {
                descriptor.withDepthAttachment(this.owner.depthView(),
                        this.clear ? OptionalDouble.of(1.0) : OptionalDouble.empty());
            }

            final CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            try (RenderPass pass = encoder.createRenderPass(descriptor.build())) {
                RenderSystem.bindDefaultUniforms(pass);
                FeatureRenderDispatcher.renderAllFeatures(pass, prepared);
            } finally {
                prepared.close();
            }
        }

        @Override
        public void close() {
            this.draw();
        }
    }
}
