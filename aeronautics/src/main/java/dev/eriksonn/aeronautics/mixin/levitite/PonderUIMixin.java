package dev.eriksonn.aeronautics.mixin.levitite;

import dev.eriksonn.aeronautics.content.blocks.levitite.LevititeShaderManager;
import com.zurrtum.create.client.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PonderUI.class)
public class PonderUIMixin {
    // 26.3: renderScene(GuiGraphicsExtractor, int, int, float, float) —— 不再有 Window 参数
    @Inject(method = "renderScene", at = @At("HEAD"))
    protected void renderScene(GuiGraphicsExtractor graphics, int id, int index, float partialTicks,
                               float uiTicks, CallbackInfo ci) {
        LevititeShaderManager.disableShader();
    }
}
