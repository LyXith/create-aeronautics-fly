package dev.simulated_team.simulated.util;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import foundry.veil.api.client.render.VeilLevelPerspectiveRenderer;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.impl.client.render.perspective.LevelPerspectiveCamera;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;

import java.util.Collection;

/**
 * Renders a chain of sub-levels into an off-screen target for the diagram screen.
 *
 * <p>26.3 port: the old {@code SubmitNodeCollector} + {@code BlockRenderDispatcher}
 * pair is gone. Models are now registered on a {@link SubmitNodeStorage}, which
 * {@link AdvancedFbo.Pass} feeds through the vanilla feature dispatcher and executes
 * in a {@code RenderPass} bound to the target's attachments.
 */
public class SimpleSubLevelGroupRenderer {
    private static final LevelPerspectiveCamera CAMERA = new LevelPerspectiveCamera();
    private static final ProjectionMatrixBuffer PROJECTION = new ProjectionMatrixBuffer("Simulated diagram projection");
    private static final Matrix4f TRANSFORM = new Matrix4f();
    public static boolean RENDERING_SIMPLE = false;

    /**
     * @return the chain of sub-levels that should render with a given sub-level into a diagram
     */
    public static Collection<ClientSubLevel> getRenderedChain(final ClientSubLevel subLevel) {
        final ObjectOpenHashSet<ClientSubLevel> visited = new ObjectOpenHashSet<>();
        final ObjectOpenHashSet<ClientSubLevel> frontier = new ObjectOpenHashSet<>();

        frontier.add(subLevel);

        while (!frontier.isEmpty()) {
            final ClientSubLevel current = frontier.iterator().next();

            frontier.remove(current);
            visited.add(current);

            final Iterable<SubLevel> intersecting = Sable.HELPER.getAllIntersecting(current.getLevel(), new BoundingBox3d(current.boundingBox()));

            // Intersecting dependencies
            for (final SubLevel neighbor : intersecting) {
                final ClientSubLevel serverNeighbor = (ClientSubLevel) neighbor;

                if (!visited.contains(serverNeighbor)) {
                    frontier.add(serverNeighbor);
                }
            }
        }

        return visited;
    }

    public static void renderChain(final SubLevel subLevel, final AdvancedFbo fbo, final Matrix4f modelView, final Matrix4f projectionMat, final Vector3d cameraPosition, final Quaternionf orientation, final float partialTicks) {
        final ClientSubLevel clientSubLevel = (ClientSubLevel) subLevel;
        final ClientLevel level = clientSubLevel.getLevel();
        final Collection<ClientSubLevel> subLevels = SimpleSubLevelGroupRenderer.getRenderedChain(clientSubLevel);

        renderGroup(level, subLevels, fbo, modelView, projectionMat, cameraPosition, orientation, partialTicks, true);
    }

    public static void renderGroup(final ClientLevel level, final Collection<ClientSubLevel> subLevels, final AdvancedFbo fbo, final Matrix4f modelView, final Matrix4f projectionMat, final Vector3d cameraPosition, final Quaternionf orientation, final float partialTicks, final boolean renderPlayers) {
        final Minecraft minecraft = Minecraft.getInstance();

        CAMERA.setup(cameraPosition, null, minecraft.level, orientation, 0f);

        final PoseStack poseStack = new PoseStack();
        poseStack.rotate(TRANSFORM.set(modelView));
        poseStack.rotate(CAMERA.rotation());

        // The feature dispatcher snapshots these while preparing the frame, so both
        // must be in place before the pass draws (not merely before it submits).
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(PROJECTION.getBuffer(projectionMat), ProjectionType.ORTHOGRAPHIC);

        final Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.identity();
        matrix4fstack.mul(poseStack.last().pose());

        RENDERING_SIMPLE = true;
        VeilLevelPerspectiveRenderer.setRenderingPerspective(true);
        try (AdvancedFbo.Pass pass = fbo.begin(true)) {
            // An empty chain still clears the target: the diagram must not keep stale
            // geometry from the previous frame.
            if (subLevels.isEmpty()) {
                return;
            }

            final SubmitNodeStorage submits = pass.collector();
            final BlockModelResolver modelResolver = new BlockModelResolver(minecraft.getModelManager());
            final BlockModelRenderState modelState = new BlockModelRenderState();
            final BlockDisplayContext displayContext = BlockDisplayContext.create();

            for (final ClientSubLevel renderedSubLevel : subLevels) {
                final SubLevelRenderData renderData = renderedSubLevel.getRenderData();
                final Vector3d chunkOffset = renderData.getChunkOffset();
                final PoseStack blockPoseStack = new PoseStack();
                blockPoseStack.rotate(renderData.getTransformation(cameraPosition.x, cameraPosition.y, cameraPosition.z));
                blockPoseStack.translate(chunkOffset.x, chunkOffset.y, chunkOffset.z);

                final var bounds = renderedSubLevel.getPlot().getBoundingBox();
                for (final BlockPos blockPos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                    final BlockState blockState = renderedSubLevel.getLevel().getBlockState(blockPos);
                    if (blockState.isAir()) {
                        continue;
                    }

                    // Resolves the baked model and the chunk layer it belongs to; 26.3
                    // derives that from the model at bake time, so there is no
                    // ItemBlockRenderTypes lookup to port.
                    modelResolver.update(modelState, blockState, displayContext);

                    blockPoseStack.pushPose();
                    blockPoseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    modelState.submit(blockPoseStack, submits,
                            LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                    blockPoseStack.popPose();
                }
            }
        } finally {
            RENDERING_SIMPLE = false;
            VeilLevelPerspectiveRenderer.setRenderingPerspective(false);

            matrix4fstack.popMatrix();
            RenderSystem.restoreProjectionMatrix();

            // The lightmap is no longer mutated by this pass: 26.3 renders it from
            // LightmapRenderState once per frame in GameRenderer, and every block here
            // is submitted at FULL_BRIGHT, so nothing needs restoring.
        }
    }
}
