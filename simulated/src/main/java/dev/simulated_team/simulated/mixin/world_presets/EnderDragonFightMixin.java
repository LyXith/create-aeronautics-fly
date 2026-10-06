package dev.simulated_team.simulated.mixin.world_presets;

import dev.simulated_team.simulated.index.SimWorldPresets;
import dev.simulated_team.simulated.mixin_interface.PrimaryLevelDataExtension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the {@code simulated:end_sea} world preset to the end dragon fight.
 *
 * <p>1.21 kept an {@code EnderDragonFight.Data} on {@code PrimaryLevelData} that the
 * world-creation screen filled in, and vanilla copied it into the fight when the End
 * loaded. Minecraft 26.3 deleted that field (the fight is now a plain {@code SavedData}
 * with individual boolean fields), so the same four values are written directly onto
 * the fight the first time it is initialised for this world.
 *
 * <p>The {@code needsStateScanning} guard is what distinguishes a fight that has just
 * been created from one that was loaded from disk: {@code EnderDragonFight.createDefault}
 * starts with it set, and it is persisted as {@code false} after the first scan.
 */
@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {

    @Shadow
    @Mutable
    private boolean needsStateScanning;

    @Shadow
    @Mutable
    private boolean dragonKilled;

    @Shadow
    @Mutable
    private boolean hasPreviouslyKilledDragon;

    @Shadow
    @Mutable
    private boolean skipArenaLoadedCheck;

    @Inject(
            method = "init(Lnet/minecraft/server/level/ServerLevel;JLnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD")
    )
    private void simulated$applyWorldPreset(final ServerLevel level, final long seed, final BlockPos origin, final CallbackInfo ci) {
        if (!this.needsStateScanning) {
            // Persisted fight state; the preset has already been applied once.
            return;
        }

        final MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        final WorldData worldData = server.getWorldData();
        if (!(worldData instanceof final PrimaryLevelDataExtension extension)) {
            return;
        }

        if (!SimWorldPresets.END_SEA.id().equals(extension.getPreset())) {
            return;
        }

        // Values that used to arrive via PrimaryLevelData#endDragonFightData.
        this.needsStateScanning = false;
        this.dragonKilled = false;
        this.hasPreviouslyKilledDragon = true;
        this.skipArenaLoadedCheck = true;
    }
}
