package dev.eriksonn.aeronautics.fabric.mixin.levitite;

import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterFogEnvironment.class)
public abstract class WaterFogEnvironmentMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void aeronautics$levititeFogRange(final FogData fogData, final Camera camera,
                                               final ClientLevel level,
                                               final float renderDistance, final DeltaTracker deltaTracker,
                                               final CallbackInfo ci) {
        if (camera.entity().getFluidHeight(AeroTags.FluidTags.LEVITITE_BLEND) > 0.0D) {
            // NeoForge's fluid fog distance modifier is 0.03125, yielding a
            // dense, close-range teal fog instead of normal water visibility.
            fogData.environmentalStart = -8.0F;
            fogData.environmentalEnd = 3.0F;
            fogData.skyEnd = 3.0F;
            fogData.cloudEnd = 3.0F;
        }
    }
}
