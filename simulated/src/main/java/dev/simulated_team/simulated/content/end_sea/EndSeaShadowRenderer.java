package dev.simulated_team.simulated.content.end_sea;

import dev.simulated_team.simulated.content.blocks.void_anchor.VoidAnchorBlockEntity;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Compatibility switch for the end-sea shadow pipeline.
 *
 * <p>The shadow map itself was driven by Veil's {@code VeilRenderLevelStageEvent} bus
 * ({@code renderShadowMap}), which no longer exists in 26.3. The event bus was replaced
 * by Fabric's {@code LevelRenderEvents}; the stage callback had no callers and no
 * framebuffer to render into, so it was dropped with the Veil event rather than left
 * behind as a stub. Everything below is the part of this API that other code still queries.
 */
public final class EndSeaShadowRenderer {
    public static final float SHADOW_VOLUME_RADIUS = 128f;
    private static final Vector3dc ORIGIN = new Vector3d();

    private EndSeaShadowRenderer() {
    }

    public static boolean isEnabled() {
        return false;
    }

    public static boolean renderingShadowMap() {
        return false;
    }

    public static Vector3dc getLastRenderOrigin() {
        return ORIGIN;
    }

    public static Object getShadowsFramebuffer() {
        return null;
    }

    public static void addVoidAnchor(VoidAnchorBlockEntity anchor) {
    }
}
