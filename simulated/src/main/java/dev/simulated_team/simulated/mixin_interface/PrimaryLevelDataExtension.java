package dev.simulated_team.simulated.mixin_interface;

import net.minecraft.resources.Identifier;

public interface PrimaryLevelDataExtension {
	Identifier getPreset();
	void setPreset(Identifier resourceLocation);
}
