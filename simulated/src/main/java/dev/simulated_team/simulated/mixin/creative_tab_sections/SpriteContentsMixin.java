package dev.simulated_team.simulated.mixin.creative_tab_sections;

import dev.simulated_team.simulated.mixin_interface.SpriteContentsExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin implements SpriteContentsExtension {

    @Unique
    private AnimationState simulated$ticker = null;

    @Override
    public AnimationState simulated$getTicker() {
        return this.simulated$ticker;
    }

    @Override
    public void simulated$setTicker(AnimationState ticker) {
        this.simulated$ticker = ticker;
    }

    @Inject(method = "createAnimationState", at = @At("RETURN"))
    private void simulated$createTicker(CallbackInfoReturnable<AnimationState> cir) {
        simulated$setTicker(cir.getReturnValue());
    }

}
