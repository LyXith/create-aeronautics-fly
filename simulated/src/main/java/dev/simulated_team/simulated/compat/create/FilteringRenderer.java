package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.BlockItem;

public final class FilteringRenderer {
    private FilteringRenderer() {
    }

    public static void renderOnBlockEntity(final SmartBlockEntity blockEntity, final float partialTicks, final PoseStack poseStack,
                                           final SubmitNodeCollector buffers, final int light, final int overlay) {
        for (final com.zurrtum.create.api.behaviour.BlockEntityBehaviour<?> behaviour : blockEntity.getAllBehaviours()) {
            if (!(behaviour instanceof final FilteringBehaviour filtering) || !(filtering.getFilter().getItem() instanceof final BlockItem blockItem)) {
                continue;
            }

            poseStack.pushPose();
            filtering.getSlotPositioning().transform(blockEntity.getBlockState(), poseStack);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            CachedBuffers.block(blockItem.getBlock().defaultBlockState()).light(light)
                    .submit(poseStack, buffers);
            poseStack.popPose();
        }
    }
}
