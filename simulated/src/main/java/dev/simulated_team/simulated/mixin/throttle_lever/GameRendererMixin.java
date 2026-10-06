package dev.simulated_team.simulated.mixin.throttle_lever;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlockEntity;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverClientGripHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3: 准星拾取从 GameRenderer.pick(float) 移到了 Minecraft.pick(float)，
 * 所以 mixin 的宿主也跟着换到 Minecraft（player / hitResult 都是它的公开字段）。
 */
@Mixin(Minecraft.class)
public class GameRendererMixin {

    @Shadow
    public LocalPlayer player;

    @Shadow
    public HitResult hitResult;

    @Inject(method = "pick(F)V", at = @At("TAIL"))
    private void simulated$pickThrottleLever(final float partialTicks, final CallbackInfo ci) {
        final LocalPlayer player = this.player;
        if (player == null) return;

        final Vec3 eyePos = Sable.HELPER.getEyePositionInterpolated(player, partialTicks);

        final HitResult mcHitResult = this.hitResult;
        double minDistance = mcHitResult != null && mcHitResult.getType() != HitResult.Type.MISS ? Sable.HELPER.distanceSquaredWithSubLevels(player.level(), eyePos, mcHitResult.getLocation()) : Double.MAX_VALUE;

        for (final ThrottleLeverBlockEntity lever : ThrottleLeverClientGripHandler.getNearbyThrottleLevers()) {
            if (lever.isRemoved()) continue;

            final Double hitResultDistance = ThrottleLeverClientGripHandler.raycastLever(eyePos, player.getViewVector(partialTicks), lever, partialTicks);

            if (hitResultDistance != null) {
                if (hitResultDistance < minDistance) {
                    minDistance = hitResultDistance;
                    this.hitResult = new BlockHitResult(Vec3.atCenterOf(lever.getBlockPos()), Direction.UP, lever.getBlockPos(), false);
                }
            }

        }
    }

}
