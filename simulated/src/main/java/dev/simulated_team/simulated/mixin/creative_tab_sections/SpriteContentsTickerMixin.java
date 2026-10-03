package dev.simulated_team.simulated.mixin.creative_tab_sections;

import dev.simulated_team.simulated.mixin_interface.TickerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.texture.SpriteContents.AnimationState.class)
public class SpriteContentsTickerMixin implements TickerExtension {

    @Unique
    private boolean simulated$playing = true;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void simulated$pauseAnimation(final CallbackInfo ci) {
        if (!this.simulated$playing) ci.cancel();
    }

    @Override
    public void simulated$setPlaying(boolean playing) {
        this.simulated$playing = playing;
    }

    @Override
    public boolean simulated$isPlaying() {
        return this.simulated$playing;
    }

}
