package dev.simulated_team.simulated.fabric.mixin.laser;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.content.blocks.lasers.IrisLaserRenderQueue;
import dev.simulated_team.simulated.content.blocks.lasers.LateLaserRenderQueue;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffRenderHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feeds the deferred laser / physics-staff geometry into the level's submit-node
 * storage so it is picked up by {@link FeatureRenderDispatcher#prepareFrame}.
 *
 * <p>The 26.3 frame graph no longer exposes an immediate-mode buffer source, so
 * these draws become ordinary submit nodes instead of a bespoke frame pass.</p>
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
    private void simulated$submitLateLaserGeometry(final GraphicsResourceAllocator allocator,
                                                   final boolean renderBlockOutline,
                                                   final CameraRenderState cameraState,
                                                   final GpuBufferSlice fogBuffer,
                                                   final Vector4f fogColor,
                                                   final boolean renderSky,
                                                   final boolean consistentDepthRequired,
                                                   final CallbackInfo ci) {
        LateLaserRenderQueue.drawAfterClouds(this.submitNodeStorage);
        IrisLaserRenderQueue.drawAfterShaderComposite(this.submitNodeStorage);

        final Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        final PoseStack poseStack = new PoseStack();
        poseStack.rotate(camera.rotation().conjugate(new Quaternionf()));
        PhysicsStaffRenderHandler.renderAfterShaderComposite(this.submitNodeStorage, poseStack, camera);
    }
}
