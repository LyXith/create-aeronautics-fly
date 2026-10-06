package dev.simulated_team.simulated.mixin.rope;

import dev.simulated_team.simulated.content.blocks.rope.strand.client.ZiplineClientManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3 里方块描边的提取从 {@code LevelRenderer} 挪到了 {@link LevelExtractor}，
 * 签名 {@code extractBlockOutline(Camera, LevelRenderState)} 保持不变。
 */
@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

    @Inject(method = "extractBlockOutline", at = @At("HEAD"), cancellable = true)
    private void simulated$cancelBlockHitOutline(final Camera camera, final LevelRenderState renderState, final CallbackInfo ci) {
        if (ZiplineClientManager.hoveringRope != null) {
            ci.cancel();
        }
    }
}
