package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.Simulated;
import com.zurrtum.create.client.catnip.render.BindableTexture;
import net.minecraft.resources.Identifier;

public enum SimSpecialTextures implements BindableTexture {
	HONEY_GLUE("honey_glue.png");

	public static final String ASSET_PATH = "textures/special/";
	private final Identifier location;

	SimSpecialTextures(final String filename) {
        this.location = Simulated.path(ASSET_PATH + filename);
	}

	public Identifier getLocation() {
		return this.location;
	}
}
