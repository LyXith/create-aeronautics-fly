package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * 26.3 移除了 {@code MultiBufferSource}（渲染改为 SubmitNode/RenderPipeline 架构）。
 * 本地桩实现：向其中提交的几何会被丢弃，仅保证旧渲染器代码可编译。
 */
public interface MultiBufferSource {

    VertexConsumer getBuffer(RenderType renderType);

    /** 兼容旧 {@code Minecraft#renderBuffers().bufferSource()} 调用点。 */
    static BufferSource bufferSource() {
        return Holder.BUFFER;
    }

    static BufferSource immediate() {
        return Holder.BUFFER;
    }

    final class Holder {
        private static final BufferSource BUFFER = new BufferSource() {
            private final VertexConsumer discard = new DiscardingVertexConsumer();

            @Override
            public VertexConsumer getBuffer(final RenderType renderType) {
                return this.discard;
            }

            @Override
            public void endBatch() {
            }

            @Override
            public void endBatch(final RenderType renderType) {
            }
        };

        private Holder() {
        }
    }

    interface BufferSource extends MultiBufferSource {
        void endBatch();

        void endBatch(RenderType renderType);
    }
}
