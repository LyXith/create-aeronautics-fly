package dev.simulated_team.simulated.content.worldgen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;

public class AirshipReadyPreset extends SimulatedWorldPreset {
	public AirshipReadyPreset(final Identifier id, @Nullable final Component description) {
		super(id, description);
	}

	@Override
	public void modifyGameRules(final GameRules gameRules) {
		gameRules.set(GameRules.SPAWN_MOBS, false, null);
		gameRules.set(GameRules.SPAWN_WANDERING_TRADERS, false, null);
		gameRules.set(GameRules.ADVANCE_WEATHER, false, null);
		gameRules.set(GameRules.ADVANCE_TIME, false, null);
	}
}
