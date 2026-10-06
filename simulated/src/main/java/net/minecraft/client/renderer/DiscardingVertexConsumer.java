package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * 丢弃所有顶点的 {@link VertexConsumer} 桩实现。
 */
public final class DiscardingVertexConsumer implements VertexConsumer {
    @Override
    public VertexConsumer addVertex(final float x, final float y, final float z) {
        return this;
    }

    @Override
    public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha) {
        return this;
    }

    @Override
    public VertexConsumer setColor(final int color) {
        return this;
    }

    @Override
    public VertexConsumer setUv(final float u, final float v) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(final int u, final int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(final int u, final int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv3(final float u, final float v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(final float x, final float y, final float z) {
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(final float width) {
        return this;
    }
}
