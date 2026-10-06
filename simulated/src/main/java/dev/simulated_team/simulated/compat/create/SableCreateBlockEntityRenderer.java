package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.kinetics.fan.EncasedFanRenderer;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Replays a 1.21 render-state submission directly into Sable's Sodium
 * sublevel buffer. This preserves renderer families that submit more than one
 * piece, such as chain conveyors, belts, depots, blaze burners, and compatible
 * third-party block entities.
 *
 * <p>26.3 port: the submission target used to be a {@code MultiBufferSource}, which
 * required an intermediate collector that re-implemented (and in several cases
 * silently discarded) Minecraft's own submission API. Minecraft 26.3 hands renderers a
 * {@link SubmitNodeCollector} directly, so the adapter is gone and every piece a
 * renderer submits now reaches Sable's pass untouched.</p>
 */
public final class SableCreateBlockEntityRenderer {
    private SableCreateBlockEntityRenderer() {
    }

    public static void render(
            final BlockEntity blockEntity,
            final float partialTick,
            final PoseStack poseStack,
            final SubmitNodeCollector collector,
            final int light,
            final int overlay
    ) {
        if (!(Sable.HELPER.getContaining(blockEntity)
                instanceof final ClientSubLevel subLevel)) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final BlockEntityRenderer renderer = minecraft
                .getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
        if (renderer == null) {
            // Some Create block entities, including ordinary fluid pipes, only
            // have static block-model geometry and intentionally have no BER.
            return;
        }

        final Camera camera = minecraft.gameRenderer.getMainCamera();
        final Pose3dc renderPose = subLevel.renderPose(partialTick);
        final Vec3 localCameraPosition =
                renderPose.transformPositionInverse(camera.position());
        final CameraRenderState cameraState = createCameraState(
                camera,
                renderPose,
                localCameraPosition
        );

        SableCreateRenderContext.run(() -> {
            final BlockEntityRenderState renderState =
                    (BlockEntityRenderState) renderer.createRenderState();
            renderer.extractRenderState(
                    blockEntity,
                    renderState,
                    partialTick,
                    localCameraPosition,
                    null
            );

            // Sable samples light around the transformed physical block. Keep
            // that result instead of the plot-space light extracted by vanilla.
            renderState.lightCoords = light;
            if (renderState
                    instanceof final EncasedFanRenderer.EncasedFanRenderState fanState) {
                fanState.lightBehind = light;
                fanState.lightInFront = light;
            }

            // An empty burner has no dynamic blaze geometry. Create's renderer
            // leaves a freshly allocated state empty in that case.
            if (renderState
                    instanceof final BlazeBurnerRenderer.BlazeBurnerRenderState burnerState
                    && burnerState.data == null) {
                return;
            }

            renderer.submit(renderState, poseStack, collector, cameraState);
        });
    }

    private static CameraRenderState createCameraState(
            final Camera camera,
            final Pose3dc renderPose,
            final Vec3 localCameraPosition
    ) {
        final CameraRenderState state = new CameraRenderState();
        state.pos = localCameraPosition;
        state.entityPos = localCameraPosition;
        state.blockPos = BlockPos.containing(localCameraPosition);
        state.initialized = camera.isInitialized();
        state.orientation = new Quaternionf(renderPose.orientation())
                .conjugate()
                .mul(camera.rotation());
        return state;
    }
}
