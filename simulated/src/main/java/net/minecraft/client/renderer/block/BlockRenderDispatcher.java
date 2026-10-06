package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 26.3 移除了 {@code BlockRenderDispatcher}（Minecraft#getBlockRenderer 同时被移除）。
 * 桩实现不写入任何几何。
 */
public class BlockRenderDispatcher {
    private static final BlockRenderDispatcher INSTANCE = new BlockRenderDispatcher();

    public static BlockRenderDispatcher getInstance() {
        return INSTANCE;
    }

    public void renderSingleBlock(final BlockState state, final PoseStack poseStack,
                                  final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
    }
}
