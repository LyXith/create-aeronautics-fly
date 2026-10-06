package net.minecraft.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 26.3 移除了 {@code ItemBlockRenderTypes}。桩实现返回移动方块渲染类型。
 */
public final class ItemBlockRenderTypes {
    private ItemBlockRenderTypes() {
    }

    public static RenderType getMovingBlockRenderType(final BlockState state) {
        if (state == null) {
            return RenderTypes.cutoutMovingBlock();
        }
        return RenderTypes.translucentMovingBlock();
    }

    public static RenderType getChunkRenderType(final BlockState state) {
        return getMovingBlockRenderType(state);
    }
}
