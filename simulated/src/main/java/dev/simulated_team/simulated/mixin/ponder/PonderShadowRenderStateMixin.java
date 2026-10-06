package dev.simulated_team.simulated.mixin.ponder;

import dev.simulated_team.simulated.mixin_interface.ponder.PonderShadowState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * 平台阴影的淡出。
 *
 * <p>26.3 把 {@code 0x66000000}（不透明度 0x66 的纯黑）从
 * {@code SceneRenderer.renderScene} 挪到了内部 record 的 {@code addBlackVertex}，
 * 所以注入点跟着搬过来；淡出系数由 {@link PonderUIMixin} 写进
 * {@link PonderShadowState#fade}。
 */
@Mixin(targets = "com.zurrtum.create.client.ponder.foundation.render.SceneRenderer$ShadowRenderState")
public class PonderShadowRenderStateMixin {

    @ModifyConstant(method = "addBlackVertex", constant = @Constant(intValue = 0x66_000000))
    private static int simulated$shadowFade(final int constant) {
        final int alpha = (int) ((constant >>> 24) * PonderShadowState.fade);
        return (alpha << 24) | (constant & 0x00FFFFFF);
    }
}
