package dev.simulated_team.simulated.mixin.sable_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;

@Mixin(value = PlacementClient.class, remap = false)
public abstract class PlacementClientMixin {
    // Class literals remap the Minecraft types even though Create's method names stay unchanged.
    @WrapOperation(method = "drawDirectionIndicator", at = @At(value = "INVOKE", desc = @Desc(
            owner = VecHelper.class, value = "projectToPlayerView", args = {Vec3.class, float.class}, ret = Vec3.class)))
    private static Vec3 simulated$projectPhysicalPlacementTarget(final Vec3 target, final float partialTick,
                                                                final Operation<Vec3> original) {
        final var level = Minecraft.getInstance().level;
        if (level != null && Sable.HELPER.getContaining(level, target) instanceof final ClientSubLevel subLevel) {
            return original.call(subLevel.renderPose(partialTick).transformPosition(target), partialTick);
        }
        return original.call(target, partialTick);
    }
}
