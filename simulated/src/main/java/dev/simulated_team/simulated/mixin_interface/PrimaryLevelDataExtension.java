package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public interface PrimaryLevelDataExtension {
	Identifier getPreset();
	void setPreset(Identifier resourceLocation);
	void setEndDragonFight(EndDragonFight.Data endDragonFight);
}
