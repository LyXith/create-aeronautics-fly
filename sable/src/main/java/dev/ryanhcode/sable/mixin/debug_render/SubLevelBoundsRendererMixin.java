package dev.ryanhcode.sable.mixin.debug_render;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.network.client.ClientSableInterpolationState;
import dev.ryanhcode.sable.network.client.SubLevelSnapshotInterpolator;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class SubLevelBoundsRendererMixin {

    @Inject(method = "emitGizmos", at = @At("TAIL"))
    private void sable$renderSubLevelBounds(final Frustum frustum, final double cameraX,
                                            final double cameraY, final double cameraZ,
                                            final float partialTick, final CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.showOnlyReducedInfo()
                || !minecraft.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)
                || minecraft.level == null) {
            return;
        }
        final SubLevelContainer container = SubLevelContainer.getContainer(minecraft.level);
        if (container == null) {
            return;
        }
        for (final SubLevel subLevel : container.getAllSubLevels()) {
            if (!(subLevel instanceof final ClientSubLevel clientSubLevel)) {
                continue;
            }
            final BoundingBox3dc bounds = subLevel.boundingBox();
            Gizmos.cuboid(new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
                    bounds.maxX(), bounds.maxY(), bounds.maxZ()), GizmoStyle.stroke(0xb3808080));

            final Pose3dc pose = clientSubLevel.renderPose();
            final Vector3dc center = pose.rotationPoint();
            sable$orientedBox(new AABB(center.x() - 0.125, center.y() - 0.125, center.z() - 0.125,
                    center.x() + 0.125, center.y() + 0.125, center.z() + 0.125), pose, 0xffb3b380);
            final BoundingBox3ic plot = subLevel.getPlot().getBoundingBox();
            sable$orientedBox(new AABB(plot.minX(), plot.minY(), plot.minZ(),
                    plot.maxX() + 1.0, plot.maxY() + 1.0, plot.maxZ() + 1.0), pose, 0xffe68080);

            if (ClientSableInterpolationState.RENDER_INTERPOLATION_BOUNDS) {
                final Vector3d halfSize = bounds.size(new Vector3d()).mul(0.5);
                for (final SubLevelSnapshotInterpolator.Snapshot snapshot : clientSubLevel.getInterpolator().buffer) {
                    final Vector3dc position = snapshot.pose().position();
                    Gizmos.cuboid(new AABB(position.x() - halfSize.x, position.y() - halfSize.y,
                            position.z() - halfSize.z, position.x() + halfSize.x,
                            position.y() + halfSize.y, position.z() + halfSize.z),
                            GizmoStyle.stroke(0x8000ffff));
                }
            }
        }
    }

    @Unique
    private static void sable$orientedBox(final AABB box, final Pose3dc pose, final int color) {
        final Vec3[] corners = new Vec3[8];
        for (int index = 0; index < corners.length; index++) {
            corners[index] = pose.transformPosition(new Vec3(
                    (index & 1) == 0 ? box.minX : box.maxX,
                    (index & 2) == 0 ? box.minY : box.maxY,
                    (index & 4) == 0 ? box.minZ : box.maxZ));
        }
        Gizmos.addGizmo((primitives, alpha) -> {
            final int fadedColor = ARGB.multiplyAlpha(color, alpha);
            for (int index = 0; index < corners.length; index++) {
                for (int axis = 1; axis <= 4; axis <<= 1) {
                    if ((index & axis) == 0) {
                        primitives.addLine(corners[index], corners[index | axis], fadedColor, 1.0f);
                    }
                }
            }
        });
    }
}
