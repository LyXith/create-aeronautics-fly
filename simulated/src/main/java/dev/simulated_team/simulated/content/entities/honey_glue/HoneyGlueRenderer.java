package dev.simulated_team.simulated.content.entities.honey_glue;

import net.minecraft.client.renderer.culling.Frustum;
import dev.simulated_team.simulated.compat.minecraft.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class HoneyGlueRenderer extends EntityRenderer<HoneyGlueEntity> {

	public HoneyGlueRenderer(final EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(final HoneyGlueEntity entity) {
		return Identifier.parse("");
	}

	@Override
	public boolean shouldRender(final HoneyGlueEntity entity, final Frustum frustum, final double x, final double y, final double z) {
		return false;
	}

}
