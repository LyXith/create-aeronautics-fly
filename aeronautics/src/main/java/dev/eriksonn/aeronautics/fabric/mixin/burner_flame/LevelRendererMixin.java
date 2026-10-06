package dev.eriksonn.aeronautics.fabric.mixin.burner_flame;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.IrisBurnerFlameRenderQueue;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feeds the deferred burner-flame geometry into the level's submit-node storage
 * so it is picked up by {@link FeatureRenderDispatcher#prepareFrame}.
 *
 * <p>The 26.3 frame graph no longer exposes an immediate-mode buffer source, so
 * these draws become ordinary submit nodes instead of a bespoke frame pass —
 * the same mechanism {@code simulated} uses for its Iris laser queue.</p>
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;prepareFrame(" +
                            "Lnet/minecraft/client/renderer/SubmitNodeStorage;)" +
                            "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;"
            )
    )
    private void aeronautics$submitBurnerFlames(final GraphicsResourceAllocator allocator,
                                                final boolean renderBlockOutline,
                                                final CameraRenderState cameraState,
                                                final GpuBufferSlice fogBuffer,
                                                final Vector4f fogColor,
                                                final boolean renderSky,
                                                final CallbackInfo ci) {
        IrisBurnerFlameRenderQueue.drawAfterShaderComposite(this.submitNodeStorage);
    }
}
