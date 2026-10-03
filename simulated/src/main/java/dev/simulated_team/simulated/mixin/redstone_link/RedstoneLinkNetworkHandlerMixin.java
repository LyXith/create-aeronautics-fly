package dev.simulated_team.simulated.mixin.redstone_link;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.redstone.link.IRedstoneLinkable;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RedstoneLinkNetworkHandler.class, remap = false)
public abstract class RedstoneLinkNetworkHandlerMixin {
    @WrapOperation(method = "updateNetworkOf", at = @At(value = "INVOKE", target =
            "Lcom/zurrtum/create/content/redstone/link/RedstoneLinkNetworkHandler;withinRange(Lcom/zurrtum/create/content/redstone/link/IRedstoneLinkable;Lcom/zurrtum/create/content/redstone/link/IRedstoneLinkable;)Z"))
    private boolean simulated$physicalLinkRange(final IRedstoneLinkable from, final IRedstoneLinkable to,
                                                final Operation<Boolean> original,
                                                @Local(argsOnly = true) final LevelAccessor world) {
        if (from == to || !(world instanceof final Level level)) {
            return original.call(from, to);
        }
        final Vec3 fromPosition = Sable.HELPER.projectOutOfSubLevel(level, Vec3.atCenterOf(from.getLocation()));
        final Vec3 toPosition = Sable.HELPER.projectOutOfSubLevel(level, Vec3.atCenterOf(to.getLocation()));
        final double range = AllConfigs.server().logistics.linkRange.get();
        return fromPosition.distanceToSqr(toPosition) < range * range;
    }
}
