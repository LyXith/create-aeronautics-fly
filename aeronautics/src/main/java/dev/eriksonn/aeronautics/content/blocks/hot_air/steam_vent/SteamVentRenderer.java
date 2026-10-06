package dev.eriksonn.aeronautics.content.blocks.hot_air.steam_vent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.simulated_team.simulated.util.SimColors;
import dev.eriksonn.aeronautics.content.blocks.hot_air.GasEmitterRenderHandler;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class SteamVentRenderer extends SmartBlockEntityRenderer<SteamVentBlockEntity> {

    public SteamVentRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final SteamVentBlockEntity blockEntity, final float partialTicks, final PoseStack ms, final SubmitNodeCollector buffer, final int light, final int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        final float signalStrength = Math.max(0, blockEntity.signalStrength / 15F);

        final BlockState state = blockEntity.getBlockState();
        CachedBuffers
                .partial(AeroPartialModels.STEAM_VENT_REDSTONE, state)
                .light(light)
                .color(SimColors.redstone(signalStrength))
                .submit(RenderTypes.cutoutMovingBlock(), ms, buffer);

        final GasEmitterRenderHandler renderHandler = blockEntity.getRenderHandler();
        final int alpha = renderHandler.getAlpha(partialTicks);

        if (alpha > 2) {

            final float position = renderHandler.getPosition(partialTicks);
            final SuperByteBuffer base = CachedBuffers.partial(AeroPartialModels.STEAM_VENT_BASE, state);
            final SuperByteBuffer jet = CachedBuffers.partial(AeroPartialModels.STEAM_VENT_JET, state);

            ms.pushPose();

            base.disableDiffuse()
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .color(255, 255, 255, alpha)
                    .submit(RenderTypes.translucentMovingBlock(), ms, buffer);

            ms.translate(0.0f, (position - 1) / 3.0f, 0.0f);
            jet.disableDiffuse()
                    .light(LightCoordsUtil.FULL_BRIGHT)
                    .color(255, 255, 255, alpha)
                    .submit(RenderTypes.translucentMovingBlock(), ms, buffer);

            ms.popPose();
        }
    }
}
