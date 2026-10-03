package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.client.renderer.texture.SpriteTicker;

public interface SpriteContentsExtension {
    SpriteTicker simulated$getTicker();
    void simulated$setTicker(SpriteTicker ticker);
}
