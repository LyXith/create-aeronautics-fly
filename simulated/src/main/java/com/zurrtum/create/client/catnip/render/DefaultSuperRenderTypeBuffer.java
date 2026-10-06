package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.DiscardingVertexConsumer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * 26.3 版 Create 已删除 {@code DefaultSuperRenderTypeBuffer}。桩实现，所有几何提交均被丢弃。
 */
public class DefaultSuperRenderTypeBuffer implements SuperRenderTypeBuffer {
    private static final DefaultSuperRenderTypeBuffer INSTANCE = new DefaultSuperRenderTypeBuffer();
    private final VertexConsumer discard = new DiscardingVertexConsumer();

    public static DefaultSuperRenderTypeBuffer getInstance() {
        return INSTANCE;
    }

    @Override
    public VertexConsumer getBuffer(final RenderType renderType) {
        return this.discard;
    }

    @Override
    public VertexConsumer getEarlyBuffer(final ChunkSectionLayer layer) {
        return this.discard;
    }

    @Override
    public VertexConsumer getEarlyBuffer(final RenderType renderType) {
        return this.discard;
    }

    @Override
    public VertexConsumer getLateBuffer(final RenderType renderType) {
        return this.discard;
    }

    @Override
    public void renderStitchedSetup() {
    }

    @Override
    public void draw() {
    }

    public static final class Dispatcher {
    }
}
