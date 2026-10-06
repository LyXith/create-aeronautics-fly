package foundry.veil.api.client.render.post;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * A registered post-processing effect, executed as a single full-screen draw.
 *
 * <p>Veil ran these through an OpenGL "pinwheel" chain with a bound source and
 * destination framebuffer. 26.3 has no bound framebuffer concept, so the pass is
 * recorded directly: allocate a {@link RenderPass} over the output attachment, bind
 * the parsed std140 uniform blocks and the input textures, then draw one triangle
 * (the vertex shader expands it over the viewport, exactly like vanilla's post passes).
 */
public final class PostPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger("SimulatedPost");
    private static final Vector4f CLEAR_COLOR = new Vector4f(0f, 0f, 0f, 0f);
    private static final String UNIFORM_USAGE_LABEL = "post-uniform-";

    private final PostPipelineSpec spec;
    private final RenderPipeline pipeline;

    private final Map<String, GpuBuffer> uniformBuffers = new HashMap<>();
    private ShaderProgram program;
    private boolean missingPipelineLogged;

    public PostPipeline(final PostPipelineSpec spec) {
        this.spec = spec;

        final BindGroupLayout.Builder layout = BindGroupLayout.builder();
        for (final String block : spec.uniformBlocks()) {
            layout.withUniform(block, UniformType.UNIFORM_BUFFER);
        }
        layout.withUniform(spec.colorSampler(), UniformType.COMBINED_IMAGE_SAMPLER);
        if (spec.depthSampler() != null) {
            layout.withUniform(spec.depthSampler(), UniformType.COMBINED_IMAGE_SAMPLER);
        }
        for (final String sampler : spec.textures().keySet()) {
            layout.withUniform(sampler, UniformType.COMBINED_IMAGE_SAMPLER);
        }

        final Identifier shader = Identifier.fromNamespaceAndPath(
                spec.location().getNamespace(), "post/" + spec.location().getPath());

        this.pipeline = RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(
                        spec.location().getNamespace(), "pipeline/post/" + spec.location().getPath()))
                .withVertexShader(shader)
                .withFragmentShader(shader)
                .withBindGroupLayout(layout.build())
                .withColorTargetState(new com.mojang.renderpearl.api.pipeline.ColorTargetState(spec.blend()))
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                .withCull(false)
                .withPrimitiveTopology(com.mojang.renderpearl.api.pipeline.PrimitiveTopology.TRIANGLES)
                .build());
    }

    public Identifier location() {
        return this.spec.location();
    }

    public PostPipelineSpec spec() {
        return this.spec;
    }

    /**
     * @return the staged uniform, or {@code null} when neither shader stage declares it
     */
    public ShaderUniform getUniform(final String name) {
        final ShaderProgram program = this.program();
        return program == null ? null : program.getUniform(name);
    }

    /**
     * @return the staged uniform, failing loudly when the shader does not declare it
     */
    public ShaderUniform getUniformSafe(final String name) {
        final ShaderUniform uniform = this.getUniform(name);
        if (uniform == null) {
            throw new IllegalStateException(
                    "Post effect " + this.spec.location() + " declares no uniform named " + name);
        }
        return uniform;
    }

    /** Records and executes the full-screen pass for the framebuffers bound in {@code context}. */
    public void run(final Context context, final boolean clear) {
        final AdvancedFbo input = context.getFramebuffer(this.spec.inputFramebuffer());
        final AdvancedFbo output = context.getFramebuffer(this.spec.outputFramebuffer());
        if (input == null || output == null) {
            throw new IllegalStateException("Post effect " + this.spec.location()
                    + " is missing a framebuffer binding (input=" + this.spec.inputFramebuffer()
                    + ", output=" + this.spec.outputFramebuffer() + ")");
        }

        final CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(this.pipeline);
        if (compiled == null) {
            if (!this.missingPipelineLogged) {
                this.missingPipelineLogged = true;
                LOGGER.warn("Pipeline for post effect {} has not been compiled yet; skipping this pass",
                        this.spec.location());
            }
            return;
        }

        final CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        this.uploadUniformBlocks(encoder, output);

        final RenderPassDescriptor.Builder descriptor = RenderPassDescriptor.builder(
                        () -> "post/" + this.spec.location())
                .withColorAttachment(output.colorView(), clear ? Optional.of(CLEAR_COLOR) : Optional.empty());
        if (output.hasDepth()) {
            descriptor.withDepthAttachment(output.depthView(),
                    clear ? OptionalDouble.of(1.0) : OptionalDouble.empty());
        }

        final GpuSampler nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        try (RenderPass pass = encoder.createRenderPass(descriptor.build())) {
            pass.setPipeline(compiled);

            for (final Map.Entry<String, GpuBuffer> entry : this.uniformBuffers.entrySet()) {
                pass.setUniform(entry.getKey(), entry.getValue());
            }

            pass.setUniform(this.spec.colorSampler(), input.colorView(), nearest);
            if (this.spec.depthSampler() != null && input.hasDepth()) {
                pass.setUniform(this.spec.depthSampler(), input.depthView(), nearest);
            }

            for (final Map.Entry<String, Identifier> entry : this.spec.textures().entrySet()) {
                final AbstractTexture texture = this.texture(entry.getValue());
                pass.setUniform(entry.getKey(), texture.getTextureView(), texture.getSampler());
            }

            // The vertex shader expands three vertices across the whole viewport, so
            // there is no vertex buffer to bind.
            pass.draw(3, 1, 0, 0);
        }
    }

    private void uploadUniformBlocks(final CommandEncoder encoder, final AdvancedFbo output) {
        final ShaderProgram program = this.program();
        if (program == null) {
            return;
        }

        final ShaderUniform outSize = program.getUniform("OutSize");
        if (outSize != null) {
            outSize.setVector(output.width(), output.height());
        }

        final Map<String, ByteBuffer> blocks = program.packUniformBlocks();
        for (final Map.Entry<String, ByteBuffer> entry : blocks.entrySet()) {
            final ByteBuffer bytes = entry.getValue();
            final GpuBuffer buffer = this.uniformBuffers.computeIfAbsent(entry.getKey(), name ->
                    RenderSystem.getDevice().createBuffer(
                            () -> UNIFORM_USAGE_LABEL + this.spec.location() + "/" + name,
                            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                            Math.max(bytes.capacity(), 16)));
            encoder.writeToBuffer(buffer.slice(), bytes);
        }
    }

    private ShaderProgram program() {
        if (this.program == null) {
            final Identifier shader = Identifier.fromNamespaceAndPath(
                    this.spec.location().getNamespace(), "post/" + this.spec.location().getPath());
            try {
                this.program = ShaderProgram.compile(Minecraft.getInstance().getResourceManager(), shader);
            } catch (final RuntimeException e) {
                LOGGER.error("Failed to parse shader for post effect {}", this.spec.location(), e);
                throw e;
            }
        }
        return this.program;
    }

    private AbstractTexture texture(final Identifier location) {
        final TextureManager textures = Minecraft.getInstance().getTextureManager();
        final AbstractTexture existing = textures.getTexture(location);
        if (existing != null) {
            return existing;
        }

        final SimpleTexture texture = new SimpleTexture(location);
        textures.registerAndLoad(location, texture);
        return texture;
    }

    /** Framebuffer bindings for a single {@link #run(Context, boolean)} invocation. */
    public static final class Context {
        private final Map<Identifier, AdvancedFbo> framebuffers = new HashMap<>();

        public void setFramebuffer(final Identifier id, final AdvancedFbo fbo) {
            this.framebuffers.put(id, fbo);
        }

        public AdvancedFbo getFramebuffer(final Identifier id) {
            return this.framebuffers.get(id);
        }

        public void clearBindings() {
            this.framebuffers.clear();
        }
    }
}
