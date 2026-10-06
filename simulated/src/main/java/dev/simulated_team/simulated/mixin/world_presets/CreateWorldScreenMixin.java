package dev.simulated_team.simulated.mixin.world_presets;

import dev.simulated_team.simulated.content.worldgen.SimulatedWorldPreset;
import dev.simulated_team.simulated.index.SimWorldPresets;
import dev.simulated_team.simulated.mixin_interface.PrimaryLevelDataExtension;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Shadow
    @Final
    WorldCreationUiState uiState;

    @Inject(method = "createNewWorld", at = @At("HEAD"))
    private void simulated$createNewWorld(final CallbackInfoReturnable<Boolean> cir) {
        final Holder<WorldPreset> holder = this.uiState.getWorldType().preset();
        if (holder == null) {
            return;
        }

        final Optional<ResourceKey<WorldPreset>> key = holder.unwrapKey();
        if (key.isEmpty()) {
            return;
        }

        final Identifier location = key.get().identifier();
        final SimulatedWorldPreset simPreset = SimWorldPresets.PRESETS.get(location);

        if (simPreset != null) {
            final GameRules gameRules = this.uiState.getGameRules();
            simPreset.modifyGameRules(gameRules);
        }
    }

    // 26.3: createNewWorld 不再有 WorldData 局部变量 —— LevelData 已经被收进第二个参数
    // LevelDataAndDimensions.WorldDataAndGenSettings（里面就是 new PrimaryLevelData(...)）。
    // 所以这里改用目标方法的形参取数据，而不是 @Local 抓局部变量。
    @Inject(method = "createNewWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createWorldOpenFlows()Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;", shift = At.Shift.BEFORE))
    private void simulated$createNewWorld2(final LayeredRegistryAccess<?> finalLayers,
                                           final LevelDataAndDimensions.WorldDataAndGenSettings worldDataAndGenSettings,
                                           final Optional<GameRules> gameRules,
                                           final CallbackInfoReturnable<Boolean> cir) {
        final Holder<WorldPreset> holder = this.uiState.getWorldType().preset();
        if (holder == null) {
            return;
        }

        final Optional<ResourceKey<WorldPreset>> key = holder.unwrapKey();
        if (key.isEmpty()) {
            return;
        }

        ((PrimaryLevelDataExtension) worldDataAndGenSettings.data()).setPreset(key.get().identifier());
    }
}
