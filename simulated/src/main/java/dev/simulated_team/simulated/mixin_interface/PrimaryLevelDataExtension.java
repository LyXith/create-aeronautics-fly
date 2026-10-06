package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

public interface PrimaryLevelDataExtension {
	Identifier getPreset();
	void setPreset(Identifier resourceLocation);
	void setEndDragonFight(EnderDragonFight.Data endDragonFight);
}
