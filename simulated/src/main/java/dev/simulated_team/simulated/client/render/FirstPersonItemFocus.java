package dev.simulated_team.simulated.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Tracks a point on an item that a world-space effect should remain attached to.
 *
 * <p>Vanilla first-person items are rendered in a camera-local projection, while
 * first-person body mods render held items as part of the camera-relative world.
 * Keeping track of which render path supplied the point prevents applying the
 * first-person projection conversion to an already-world-relative position.</p>
 */
public final class FirstPersonItemFocus {
    private static final Matrix4f WORLD_PROJECTION = new Matrix4f();
    private static boolean hasWorldProjection;
    private final Vector3d capturedPosition = new Vector3d();
    private final Matrix4f capturedProjection = new Matrix4f();
    private boolean cameraRelativeWorldSpace;

    /**
     * Captures a point rendered by vanilla's first-person hand pass.
     */
    public void captureProjected(final PoseStack matrices, final Minecraft minecraft, final float partialTicks) {
        this.capturePosition(matrices);
        final Camera camera = minecraft.gameRenderer.getMainCamera();
        camera.rotation().transformInverse(this.capturedPosition);
        // The hand pass has its own lens; the configured world FOV does not apply to it.
        this.capturedProjection.set(minecraft.gameRenderer.getProjectionMatrix(
                minecraft.gameRenderer.getFov(camera, partialTicks, false)));
        this.cameraRelativeWorldSpace = false;
    }

    /** Records the current world lens, including view bobbing and other camera effects. */
    public static void captureWorldProjection(final Matrix4f projection) {
        WORLD_PROJECTION.set(projection);
        hasWorldProjection = true;
    }

    /**
     * Captures a point rendered with the local player's body in the world pass.
     */
    public void captureCameraRelativeWorld(final PoseStack matrices) {
        this.capturePosition(matrices);
        this.cameraRelativeWorldSpace = true;
    }

    private void capturePosition(final PoseStack matrices) {
        final Vector3f focusPoint = new Vector3f();
        matrices.last().pose().transformPosition(focusPoint);
        this.capturedPosition.set(focusPoint.x, focusPoint.y, focusPoint.z);
    }

    /**
     * Resolves the captured point as a world-oriented offset from the camera.
     *
     * @param partialTicks render partial ticks
     * @param rotateProjectedForShader whether the caller's shader render path
     *                                 applies an additional inverse camera rotation
     */
    public Vec3 resolveCameraRelative(final float partialTicks, final boolean rotateProjectedForShader) {
        final Vector3d focusPoint = new Vector3d(this.capturedPosition);
        if (this.cameraRelativeWorldSpace) {
            return JOMLConversion.toMojang(focusPoint);
        }

        final GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        final Camera camera = gameRenderer.getMainCamera();
        final Quaternionf orientation = camera.rotation();
        final Vector4f projectedPoint = new Vector4f(
                (float) focusPoint.x, (float) focusPoint.y, (float) focusPoint.z, 1.0f);
        final Matrix4f actualProjection = hasWorldProjection ? WORLD_PROJECTION
                : gameRenderer.getProjectionMatrix(gameRenderer.getFov(camera, partialTicks, true));
        // Match the hand's clip-space position using the world projection, then return
        // to world orientation. Uniformly scaling by FOV cannot change screen position.
        this.capturedProjection.transform(projectedPoint);
        actualProjection.invert(new Matrix4f()).transform(projectedPoint);

        focusPoint.set(projectedPoint.x / projectedPoint.w,
                projectedPoint.y / projectedPoint.w, projectedPoint.z / projectedPoint.w);
        orientation.transform(focusPoint);

        if (rotateProjectedForShader) {
            orientation.transform(focusPoint);
        }

        return JOMLConversion.toMojang(focusPoint);
    }

    public boolean isCameraRelativeWorldSpace() {
        return this.cameraRelativeWorldSpace;
    }

    /**
     * Returns whether an item-model update belongs to a first-person body render.
     */
    public static boolean isLocalPlayerBodyRender(
            final ItemDisplayContext displayContext,
            @Nullable final ItemOwner owner,
            final Minecraft minecraft
    ) {
        if (displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            return false;
        }

        final LocalPlayer player = minecraft.player;
        if (player == null || owner == null || owner.asLivingEntity() != player) {
            return false;
        }

        final Camera camera = minecraft.gameRenderer.getMainCamera();
        return minecraft.options.getCameraType().isFirstPerson()
                && !camera.isDetached()
                && camera.entity() == player;
    }
}
