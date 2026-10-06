package dev.simulated_team.simulated.mixin.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import dev.simulated_team.simulated.mixin_interface.ponder.PonderSceneExtension;
import dev.simulated_team.simulated.mixin_interface.ponder.PonderShadowState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 平台阴影的位移与淡出。
 *
 * <p>26.3 的变化：
 * <ul>
 *   <li>{@code SceneRenderer.renderScene} 现在是
 *       {@code (Minecraft, SubmitNodeCollector, PoseStack, PonderScene, float, int, int, double, LerpedFloat)}，
 *       注入点 handler 必须按这个签名声明参数；</li>
 *   <li>{@code UIRenderHelper.flipForGuiRender} 已不存在，阴影改由
 *       {@code pushPose(); scale(1, -1, 1); translate(...)} 组成，
 *       位移注入改挂在 {@code PoseStack.scale(FFF)} 上（仍然在 Y 翻转之前）；</li>
 *   <li>{@code 0x66000000} 常量搬到了 {@code SceneRenderer$ShadowRenderState.addBlackVertex}，
 *       由 {@link PonderShadowRenderStateMixin} 处理，这里只负责把淡出系数算好存起来。</li>
 * </ul>
 */
@Mixin(SceneRenderer.class)
public class PonderUIMixin {

    @Unique
    private static void simulated$capture(final PonderScene scene, final float partialTicks) {
        PonderShadowState.fade = ((PonderSceneExtension) scene)
                .simulated$getBasePlateAnimationTimer(partialTicks);
    }

    @Inject(method = "renderScene", at = @At("HEAD"))
    private static void simulated$shadowFade(final Minecraft minecraft, final SubmitNodeCollector collector,
                                             final PoseStack poseStack, final PonderScene scene,
                                             final float partialTicks, final int width, final int height,
                                             final double slide, final LerpedFloat finishingFlash,
                                             final CallbackInfo ci) {
        simulated$capture(scene, partialTicks);
    }

    @Inject(method = "renderScene", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 0))
    private static void simulated$shadowTranslate(final Minecraft minecraft, final SubmitNodeCollector collector,
                                                  final PoseStack poseStack, final PonderScene scene,
                                                  final float partialTicks, final int width, final int height,
                                                  final double slide, final LerpedFloat finishingFlash,
                                                  final CallbackInfo ci) {
        simulated$capture(scene, partialTicks);
        final Vec3 offset = ((PonderSceneExtension) scene).simulated$getShadowOffset(partialTicks);
        poseStack.translate(offset);
    }
}
