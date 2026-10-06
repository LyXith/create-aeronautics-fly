package dev.eriksonn.aeronautics.content.blocks.propeller.small;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public abstract class SimplePropellerRenderer<T extends BasePropellerBlockEntity> extends KineticBlockEntityRenderer<T> {

    public SimplePropellerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderSafe(final T be, final float partialTicks, final PoseStack ms, final SubmitNodeCollector buffer, final int light, final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final BlockState state = be.getBlockState();
        final Direction dir = state.getValue(BlockStateProperties.FACING);


        final SuperByteBuffer propeller = CachedBuffers.partialFacing(this.getCurrentModel(be), state);

        final float angle = this.getAngle(partialTicks, dir, be);
        kineticRotationTransform(propeller, be, dir.getAxis(), angle, light);

        if (dir.getAxis().isHorizontal()) {
            propeller.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(dir.getOpposite())), Direction.UP);
        }
        if (dir.getAxis().isVertical()) {
            propeller.rotateCentered(AngleHelper.rad(AngleHelper.verticalAngle(dir.getOpposite())), Direction.EAST);
        }

        propeller.translate(0, 0, -3 / 16f).rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(dir)), Direction.EAST);

        propeller.submit(RenderTypes.solidMovingBlock(), ms, buffer);
    }

    public abstract PartialModel getCurrentModel(T be);

    public float getAngle(final float partialTicks, final Direction dir, final T be) {
        float angle = be.getPreviousAngle() * (1f - partialTicks) + be.getAngle() * partialTicks;

        angle = angle / 180f * (float) Math.PI;

        angle *= 2;

        return angle;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final T be, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(BearingBlock.FACING)
                .getOpposite());
    }
}
