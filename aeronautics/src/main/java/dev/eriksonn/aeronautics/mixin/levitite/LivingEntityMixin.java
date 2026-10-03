package dev.eriksonn.aeronautics.mixin.levitite;

import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies Levitite Blend's buoyant swimming movement, matching the NeoForge fluid type. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    protected boolean jumping;

    @Shadow
    protected abstract double getEffectiveGravity();

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void aeronautics$travelInLevitite(final Vec3 input, final CallbackInfo ci) {
        final LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.isAffectedByFluids()) {
            return;
        }

        // Keep vanilla water/lava precedence when the entity overlaps another fluid.
        final FluidState blockFluid = entity.level().getFluidState(entity.blockPosition());
        if (entity.isInWater() || entity.isInLava() || entity.canStandOnFluid(blockFluid)) {
            return;
        }

        final double fluidHeight = entity.getFluidHeight(AeroTags.FluidTags.LEVITITE_BLEND);
        if (fluidHeight <= 0.0D) {
            return;
        }

        final boolean falling = entity.getDeltaMovement().y <= 0.0D;
        final double oldY = entity.getY();
        double gravity = this.getEffectiveGravity();
        if (!entity.isCrouching()) {
            gravity = Math.clamp(gravity * (1.0D - fluidHeight), 0.0D, gravity);
        }

        if (this.jumping) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
        }

        entity.moveRelative(0.02F, input);
        entity.move(MoverType.SELF, entity.getDeltaMovement());

        if (fluidHeight <= entity.getFluidJumpThreshold()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, 0.7D, 0.5D));
            entity.setDeltaMovement(entity.getFluidFallingAdjustedMovement(
                    gravity, falling, entity.getDeltaMovement()));
        } else {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
        }

        if (gravity != 0.0D) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -gravity / 4.0D, 0.0D));
        }

        final Vec3 movement = entity.getDeltaMovement();
        if (entity.horizontalCollision
                && entity.isFree(movement.x, movement.y + 0.6D - entity.getY() + oldY, movement.z)) {
            entity.setDeltaMovement(movement.x, 0.3D, movement.z);
        }
        ci.cancel();
    }
}
