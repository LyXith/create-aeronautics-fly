package dev.simulated_team.simulated.mixin.spring_item_bounce;

import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract void playSound(SoundEvent soundEvent, float f, float g);

    @Shadow private BlockPos blockPosition;

    @Shadow private Level level;

    @Shadow public abstract BlockPos blockPosition();

    @Shadow public abstract Level level();

    @Shadow public abstract Vec3 getPosition(float partialTicks);

    // 26.3 把 Block#updateEntityMovementAfterFallOn 合并进了碰撞回弹流程：
    // Entity#restituteMovementAfterCollisions 计算完碰撞后的速度后统一 setDeltaMovement。
    // 因此这里改为重定向那次 setDeltaMovement —— 只有贴地（onGround）时才反转 Y，
    // 与原先「落地时被调用一次」的语义保持一致。
    @Redirect(method = "restituteMovementAfterCollisions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void simulated$bounceSpringItem(final Entity instance, final Vec3 velocity) {
        if (instance.onGround() && instance instanceof final ItemEntity item && item.getItem().is(SimItems.SPRING.get())) {
            instance.setDeltaMovement(instance.getDeltaMovement().multiply(1, -1, 1));
            return;
        }

        instance.setDeltaMovement(velocity);
    }

    @Inject(method = "checkFallDamage", at = @At(value = "HEAD"))
    private void awardAdvancementBeforeFallReset(final double d, final boolean bl, final BlockState blockState, final BlockPos blockPos, final CallbackInfo ci) {
        if (bl && ((Entity)(Object)this) instanceof final ItemEntity item && item.getItem().is(SimItems.SPRING.get())) {
            if (item.fallDistance >= 128 && item.getOwner() instanceof final Player player) {
                SimAdvancements.MUST_COME_UP.awardTo(player);
            }
        }
    }
}
