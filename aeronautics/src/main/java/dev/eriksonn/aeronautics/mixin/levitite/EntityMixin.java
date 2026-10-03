package dev.eriksonn.aeronautics.mixin.levitite;

import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds Levitite fluid tracking and bridges eye submersion to vanilla air handling. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "updateInWaterStateAndDoFluidPushing", at = @At("TAIL"), cancellable = true)
    private void aeronautics$updateLevititeFluid(CallbackInfoReturnable<Boolean> cir) {
        final Entity entity = (Entity) (Object) this;
        // NeoForge's Levitite fluid type uses the same motion scale as vanilla's
        // non-ultra-warm water path, but in the opposite direction (upward fluid).
        final boolean inLevitite = entity.updateFluidHeightAndDoFluidPushing(
                AeroTags.FluidTags.LEVITITE_BLEND, -0.0023333333333333335D);
        if (inLevitite) {
            // Keep callers of this vanilla hook informed that the entity is in
            // a fluid (for fall-distance and other fluid interaction logic).
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isEyeInFluid", at = @At("RETURN"), cancellable = true)
    private void aeronautics$eyeInLevitite(final TagKey<Fluid> fluidTag,
                                            final CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || !FluidTags.WATER.equals(fluidTag)) {
            return;
        }

        final Entity entity = (Entity) (Object) this;
        final double eyeY = entity.getEyeY();
        final BlockPos eyePos = BlockPos.containing(entity.getX(), eyeY, entity.getZ());
        final FluidState fluidState = entity.level().getFluidState(eyePos);
        if (fluidState.is(AeroTags.FluidTags.LEVITITE_BLEND)
                && eyeY < eyePos.getY() + fluidState.getHeight(entity.level(), eyePos)) {
            // Vanilla uses this query both for drowning and for the HUD air meter.
            cir.setReturnValue(true);
        }
    }
}
