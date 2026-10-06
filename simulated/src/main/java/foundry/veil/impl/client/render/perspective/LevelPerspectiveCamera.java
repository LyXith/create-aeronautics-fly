package foundry.veil.impl.client.render.perspective;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

/**
 * A camera that is not attached to any entity, used to render a level from an
 * arbitrary perspective into an off-screen target.
 *
 * <p>Veil's version copied the values into the vanilla {@link net.minecraft.client.Camera};
 * 26.3's {@code Camera} only accepts yaw/pitch and is owned by {@code GameRenderer}, so
 * this keeps the position and orientation directly and exposes the orientation as the
 * quaternion the render pass needs.
 */
public final class LevelPerspectiveCamera {
    private final Vector3d position = new Vector3d();
    private final Quaternionf rotation = new Quaternionf();

    /**
     * @param position    eye position in level space
     * @param target      optional point to look at; when set, {@code orientation} is ignored
     * @param level       the level being rendered (unused by the quaternion path, kept so
     *                    the camera can be extended with vanilla behaviour later)
     * @param orientation camera rotation used when {@code target} is {@code null}
     * @param partialTicks partial tick used to interpolate {@code position}
     */
    public void setup(final Vector3dc position, @Nullable final BlockPos target,
                      @Nullable final ClientLevel level, final Quaternionfc orientation, final float partialTicks) {
        this.position.set(position);

        if (target == null) {
            this.rotation.set(orientation);
            return;
        }

        final Vector3f direction = new Vector3f(
                (float) (target.getX() + 0.5 - this.position.x),
                (float) (target.getY() + 0.5 - this.position.y),
                (float) (target.getZ() + 0.5 - this.position.z));
        if (direction.lengthSquared() < 1.0E-6f) {
            this.rotation.set(orientation);
            return;
        }

        // Vanilla cameras look down -Z, so aim that axis at the target.
        this.rotation.identity().rotationTo(new Vector3f(0f, 0f, -1f), direction.normalize());
    }

    /** The camera orientation to bake into the model-view matrix. */
    public Quaternionf rotation() {
        return new Quaternionf(this.rotation);
    }

    public Vector3d position() {
        return new Vector3d(this.position);
    }
}
