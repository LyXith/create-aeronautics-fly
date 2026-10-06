package dev.eriksonn.aeronautics.fabric.mixin.levitite;

import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    /**
     * 26.3 起 {@code computeFogColor} 不再返回 {@link Vector4f}，颜色改为写入最后一个出参 {@code dest}。
     */
    @Inject(method = "computeFogColor", at = @At("RETURN"))
    private void aeronautics$levititeFogColor(final Camera camera, final float partialTick,
                                               final ClientLevel level, final int renderDistance,
                                               final float darkenWorld, final Vector4f dest,
                                               final CallbackInfo ci) {
        final var blockPos = camera.blockPosition();
        final var fluidState = level.getFluidState(blockPos);
        final var position = camera.position();
        if (fluidState.is(AeroTags.FluidTags.LEVITITE_BLEND)
                && position.y < blockPos.getY() + fluidState.getHeight(level, blockPos)) {
            // AeroColors.LEVIBLEND_THE_FOG_IS_COMING (16, 62, 68).
            dest.set(16.0F / 255.0F, 62.0F / 255.0F, 68.0F / 255.0F, 1.0F);
        }
    }
}
