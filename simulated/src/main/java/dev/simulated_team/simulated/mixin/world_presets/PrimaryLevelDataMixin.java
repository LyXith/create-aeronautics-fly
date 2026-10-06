package dev.simulated_team.simulated.mixin.world_presets;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import dev.simulated_team.simulated.mixin_interface.PrimaryLevelDataExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin implements PrimaryLevelDataExtension {

	@Unique
	private static final String simulated$WORLD_PRESET_KEY = "simulated:world_preset";

	private Identifier simulated$worldPresetKey = WorldPresets.NORMAL.identifier();

	@Inject(method = "parse", at = @At("RETURN"))
	private static <T> void simulated$parse(final Dynamic<T> dynamic, final LevelSettings levelSettings, final PrimaryLevelData.SpecialWorldProperty specialWorldProperty, final WorldOptions worldOptions, final Lifecycle lifecycle, final CallbackInfoReturnable<PrimaryLevelData> cir) {
		final DataResult<String> string = dynamic.get(simulated$WORLD_PRESET_KEY).asString();
		if(string.isSuccess()) {
			((PrimaryLevelDataExtension) cir.getReturnValue()).setPreset(Identifier.parse(string.getOrThrow()));
		}
	}

	@Inject(method = "setTagData", at = @At("TAIL"))
	private void simulated$setTagData(final RegistryAccess registryAccess, final CompoundTag compoundTag, final CompoundTag compoundTag2, final CallbackInfo ci) {
		compoundTag.putString(simulated$WORLD_PRESET_KEY, this.getPreset().toString());
	}

	@Override
	public Identifier getPreset() {
		return this.simulated$worldPresetKey;
	}

	@Override
	public void setPreset(final Identifier resourceLocation) {
		this.simulated$worldPresetKey = resourceLocation;
	}
}
