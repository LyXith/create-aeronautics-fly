package dev.simulated_team.simulated.fabric.mixin.physics_staff;

import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.client.render.FirstPersonItemFocus;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * 26.3 的 {@code LevelRenderer.render} 不再接收矩阵参数（矩阵收进了 {@code CameraRenderState}），
     * 所以改为在 {@code GameRenderer.renderLevel} 里、真正发起关卡渲染的那一刻，取出局部的
     * {@code projectionMatrix}。这个局部正是「视角摇晃 + 传送门/眩晕效果」之后、上传给
     * {@code ProjectionMatrixBuffer} 的世界投影矩阵，与旧版传给 {@code renderLevel} 的参数一致。
     */
    @Inject(method = "renderLevel", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render("
                    + "Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;"
                    + "Z"
                    + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;"
                    + "Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"
                    + "Lorg/joml/Vector4f;ZZ)V"
    ))
    private void simulated$captureWorldProjection(final CallbackInfo ci,
                                                  @Local(name = "projectionMatrix") final Matrix4f projection) {
        FirstPersonItemFocus.captureWorldProjection(projection);
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void simulated$replaceVanillaOutlineForPhysicsStaff(
            final CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.player != null && PhysicsStaffItem.isHolding(this.minecraft.player)) {
            cir.setReturnValue(false);
        }
    }

}
