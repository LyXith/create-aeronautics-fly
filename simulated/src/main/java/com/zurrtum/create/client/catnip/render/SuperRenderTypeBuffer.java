package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * 26.3 版 Create 已删除 {@code SuperRenderTypeBuffer}。桩接口：所有几何提交均被丢弃。
 */
public interface SuperRenderTypeBuffer extends MultiBufferSource {
    @Override
    VertexConsumer getBuffer(RenderType renderType);

    VertexConsumer getEarlyBuffer(ChunkSectionLayer layer);

    VertexConsumer getEarlyBuffer(RenderType renderType);

    VertexConsumer getLateBuffer(RenderType renderType);

    void renderStitchedSetup();

    void draw();
}
