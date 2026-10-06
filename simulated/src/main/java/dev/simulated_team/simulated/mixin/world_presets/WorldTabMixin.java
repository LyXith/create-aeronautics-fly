package dev.simulated_team.simulated.mixin.world_presets;

import dev.simulated_team.simulated.content.worldgen.SimulatedWorldPreset;
import dev.simulated_team.simulated.index.SimWorldPresets;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public class WorldTabMixin {

	// 26.3: 构造器末尾多了两个活局部变量（switchGridBuilder / switchGrid），
	// LocalCapture.CAPTURE_FAILHARD 会因为处理器签名对不上而炸掉整个类的转换。
	// 这里只需要世界类型按钮，改成按类型做隐式 @Local 匹配 —— TAIL 处活的
	// CycleButton 只有 typeButton 这一个，且完全不依赖混淆后的局部变量名。
	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void simulated$init(final CreateWorldScreen createWorldScreen, final CallbackInfo ci,
			@Local final CycleButton<WorldCreationUiState> cycleButton) {
		createWorldScreen.getUiState()
				.addListener(worldCreationUiState -> {
					final WorldCreationUiState.WorldTypeEntry worldType = worldCreationUiState.getWorldType();
					final Holder<WorldPreset> preset = worldType.preset();

					if (preset != null && preset.unwrapKey().isPresent()) {
						final ResourceKey<WorldPreset> key = preset.unwrapKey().get();
						final SimulatedWorldPreset simPreset = SimWorldPresets.PRESETS.get(key.identifier());

						if(simPreset != null && simPreset.description() != null) {
							cycleButton.setTooltip(Tooltip.create(simPreset.description()));
						}
					}
				});
	}
}
