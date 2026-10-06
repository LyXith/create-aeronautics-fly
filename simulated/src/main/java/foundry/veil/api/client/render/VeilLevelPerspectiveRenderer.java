package foundry.veil.api.client.render;

/**
 * Tracks whether a level-perspective (off-screen) render is in flight.
 *
 * <p>Veil drove this from its own level stage bus. In 26.3 the off-screen passes are
 * issued by the diagram renderer itself, so the flag is simply owned here and toggled
 * around those passes; consumers such as {@code DiagramScreen} use it as a re-entrancy
 * guard so a nested screen render cannot start a second perspective pass.
 */
public final class VeilLevelPerspectiveRenderer {
    private static boolean renderingPerspective;

    private VeilLevelPerspectiveRenderer() {
    }

    public static boolean isRenderingPerspective() {
        return renderingPerspective;
    }

    public static void setRenderingPerspective(final boolean value) {
        renderingPerspective = value;
    }
}
