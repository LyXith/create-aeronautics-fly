package dev.eriksonn.aeronautics.mixin.levitite;

import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds Levitite fluid tracking and bridges eye submersion to vanilla air handling.
 *
 * <p>26.3 把「按流体标签逐个统计液面高度、推动实体」这套逻辑搬进了
 * {@link EntityFluidInteraction}（由 {@code Entity} 持有），不再有
 * {@code updateFluidHeightAndDoFluidPushing(TagKey, double)} 这样的按标签钩子，
 * 也没有 {@code Level#random} 之类的旧入口。这里改用 26.3 的真实 API：
 * {@code isInFluid(tag)} 判断自己是否浸在 Levitite Blend 里，再按和原版
 * 非快熔岩路径相同的运动尺度、朝相反方向（向上）推动实体。</p>
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    /** Vanilla pushes fluids with {@code +0.0023333333333333335D} (non-fast lava). */
    private static final double LEVITITE_PUSH = -0.0023333333333333335D;

    @Shadow
    @Final
    private EntityFluidInteraction fluidInteraction;

    @Inject(method = "updateFluidInteraction", at = @At("TAIL"), cancellable = true)
    private void aeronautics$updateLevititeFluid(final CallbackInfoReturnable<Boolean> cir) {
        final Entity entity = (Entity) (Object) this;
        // NeoForge's Levitite fluid type uses the same motion scale as vanilla's
        // non-ultra-warm water path, but in the opposite direction (upward fluid).
        if (this.fluidInteraction.isInFluid(AeroTags.FluidTags.LEVITITE_BLEND)) {
            entity.addDeltaMovement(new Vec3(0.0, LEVITITE_PUSH, 0.0));
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
