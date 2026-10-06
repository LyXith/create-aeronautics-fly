package foundry.veil.api.client.render;

import foundry.veil.api.client.render.post.PostProcessingManager;

/**
 * Entry point to the local rendering facade that used to come from Veil.
 *
 * <p>Only the pieces this mod actually consumes are kept; the off-screen buffer
 * plumbing and the level stage bus were replaced by Minecraft's own frame graph and
 * Fabric's {@code LevelRenderEvents}.
 */
public final class VeilRenderSystem {
    private static final Renderer RENDERER = new Renderer();

    private VeilRenderSystem() {
    }

    public static Renderer renderer() {
        return RENDERER;
    }

    /** Per-client rendering state. */
    public static final class Renderer {
        private final PostProcessingManager postProcessingManager = new PostProcessingManager();

        public PostProcessingManager getPostProcessingManager() {
            return this.postProcessingManager;
        }
    }
}
