package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.client.renderer.texture.SpriteContents.AnimationState;

public interface SpriteContentsExtension {
    AnimationState simulated$getTicker();
    void simulated$setTicker(AnimationState ticker);
}
