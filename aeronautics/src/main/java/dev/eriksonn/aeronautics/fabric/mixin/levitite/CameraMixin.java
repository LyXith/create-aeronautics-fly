package dev.eriksonn.aeronautics.fabric.mixin.levitite;

import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private Vec3 position;

    @Shadow
    private @Nullable BlockGetter level;

    @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
    private void aeronautics$levititeFog(final CallbackInfoReturnable<FogType> cir) {
        if (this.level == null) {
            return;
        }

        final BlockPos blockPos = BlockPos.containing(this.position);
        final var fluidState = this.level.getFluidState(blockPos);
        if (fluidState.is(AeroTags.FluidTags.LEVITITE_BLEND)
                && this.position.y < blockPos.getY() + fluidState.getHeight(this.level, blockPos)) {
            // FogType.WATER selects the vanilla underwater rendering path; the
            // FogRenderer mixin below replaces its colour and range for Levitite.
            cir.setReturnValue(FogType.WATER);
        }
    }
}
