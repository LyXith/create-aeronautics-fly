package dev.simulated_team.simulated.mixin.creative_tab_sections;

import com.mojang.blaze3d.textures.GpuTexture;
import dev.simulated_team.simulated.mixin_interface.TickerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$Ticker")
public class SpriteContentsTickerMixin implements TickerExtension {

    @Unique
    private boolean simulated$playing = true;

    @Inject(method = "tickAndUpload", at = @At("HEAD"), cancellable = true)
    private void simulated$pauseAnimation(final int x, final int y, final GpuTexture texture, final CallbackInfo ci) {
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
