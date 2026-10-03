package dev.eriksonn.aeronautics.fabric.mixin.levitite;

import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Inject(method = "computeFogColor", at = @At("RETURN"), cancellable = true)
    private void aeronautics$levititeFogColor(final Camera camera, final float partialTick,
                                               final ClientLevel level, final int renderDistance,
                                               final float darkenWorld, final boolean bossFog,
                                               final CallbackInfoReturnable<Vector4f> cir) {
        final var blockPos = camera.getBlockPosition();
        final var fluidState = level.getFluidState(blockPos);
        final var position = camera.getPosition();
        if (fluidState.is(AeroTags.FluidTags.LEVITITE_BLEND)
                && position.y < blockPos.getY() + fluidState.getHeight(level, blockPos)) {
            // AeroColors.LEVIBLEND_THE_FOG_IS_COMING (16, 62, 68).
            cir.setReturnValue(new Vector4f(16.0F / 255.0F, 62.0F / 255.0F, 68.0F / 255.0F, 1.0F));
        }
    }
}
