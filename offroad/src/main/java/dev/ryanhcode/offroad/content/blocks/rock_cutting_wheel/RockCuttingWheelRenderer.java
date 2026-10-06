package dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.render.ContraptionMatrices;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class RockCuttingWheelRenderer extends SafeBlockEntityRenderer<RockCuttingWheelBlockEntity> {

    public RockCuttingWheelRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    private static void transformBuffer(final Direction facing, final boolean alongFirstCoords, final SuperByteBuffer wheel) {
        if ((facing.getAxis() == Direction.Axis.Z || facing.getAxis() == Direction.Axis.Y) ^ alongFirstCoords) {
            wheel.rotateCentered(facing.getRotation())
                    .rotateZCenteredDegrees(90)
                    .rotateXCenteredDegrees(0)
                    .translate(0.625, 0.5, 0);
        } else {
            wheel.rotateCentered(facing.getRotation())
                    .rotateZCenteredDegrees(0)
                    .rotateXCenteredDegrees(90)
                    .translate(0, 0.5, -0.625);
        }
    }

    public static void renderInContraption(final MovementContext context, final VirtualRenderWorld renderWorld, final ContraptionMatrices matrices, final SubmitNodeCollector buffer) {
        final BlockState state = context.state;
        final Direction facing = state.getValue(FACING);
        final SuperByteBuffer wheel = CachedBuffers.partial(OffroadPartialModels.ROCK_CUTTING_WHEEL_WHEEL, state);

        wheel.transform(matrices.getModel());

        transformBuffer(facing, state.getValue(AbstractDirectionalAxisBlock.AXIS_ALONG_FIRST_COORDINATE), wheel);
        wheel.rotateYCenteredDegrees(((LerpedFloat) context.temporaryData).getValue(AnimationTickHolder.getPartialTicks(context.world)));

        wheel.light(LightCoordsUtil.getLightCoords(renderWorld, context.localPos))
                .useLevelLight(context.world, matrices.getWorld())
                .submit(RenderTypes.solidMovingBlock(), matrices.getViewProjection(), buffer);
    }

    @Override
    protected void renderSafe(final RockCuttingWheelBlockEntity blockEntity, final float partialTicks, final PoseStack ms, final SubmitNodeCollector buffer, final int light, final int overlay) {
        final BlockState state = blockEntity.getBlockState();
        final SuperByteBuffer wheel = CachedBuffers.partial(OffroadPartialModels.ROCK_CUTTING_WHEEL_WHEEL, state);

        ms.pushPose();

        transformBuffer(state.getValue(FACING), state.getValue(AbstractDirectionalAxisBlock.AXIS_ALONG_FIRST_COORDINATE), wheel);
        if (blockEntity.isVirtual()) {
            wheel.rotateYCenteredDegrees(blockEntity.getAnimatedSpeed(partialTicks));
        }

        wheel.light(light).submit(RenderTypes.solidMovingBlock(), ms, buffer);
        ms.popPose();
    }
}
