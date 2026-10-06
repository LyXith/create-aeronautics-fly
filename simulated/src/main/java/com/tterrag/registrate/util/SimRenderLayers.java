package com.tterrag.registrate.util;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;
import java.util.function.Supplier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 26.3 中 Fabric 移除了 {@code BlockRenderLayerMap}，因为区块渲染层不再由运行时注册决定，
 * 而是由方块模型的纹理透明度在烘焙时推导出来（见 {@code FaceBakery#computeMaterialTransparency}）：
 *
 * <ul>
 * <li>纹理完全不透明 → {@link com.mojang.blaze3d.platform.Transparency#NONE} → {@link ChunkSectionLayer#SOLID}</li>
 * <li>纹理带 alpha → {@link com.mojang.blaze3d.platform.Transparency#TRANSPARENT} → {@link ChunkSectionLayer#CUTOUT}</li>
 * <li>模型纹理声明 {@code force_translucent} → {@link com.mojang.blaze3d.platform.Transparency#TRANSLUCENT} →
 * {@link ChunkSectionLayer#TRANSLUCENT}</li>
 * </ul>
 *
 * 因此 {@code BlockBuilder#addLayer} / {@code FluidBuilder#renderType} 请求的渲染层现在需要在
 * <b>数据生成</b>阶段落到模型 JSON 上，本类负责把「方块 → 渲染层」这条信息从构建期传递给
 * {@link com.tterrag.registrate.providers.RegistrateBlockstateProvider}。
 *
 * <p>注意：这里只保存 {@link NonNullSupplier}，在服务端调用时不会加载任何 {@code net.minecraft.client.*} 类。
 */
public final class SimRenderLayers {

    private static final Map<Block, NonNullSupplier<Supplier<?>>> LAYERS = new ConcurrentHashMap<>();

    private SimRenderLayers() {}

    public static void register(final Block block, final NonNullSupplier<Supplier<?>> layer) {
        if (block != null && layer != null) {
            LAYERS.put(block, layer);
        }
    }

    public static NonNullSupplier<Supplier<?>> get(final Block block) {
        return LAYERS.get(block);
    }

    /**
     * 把构建期传入的「渲染层描述符」（{@code RenderTypes.*MovingBlock()} 或 {@link ChunkSectionLayer}）
     * 翻译成 26.3 的 {@link ChunkSectionLayer}。仅可在客户端 / 数据生成环境调用。
     */
    public static ChunkSectionLayer toChunkSectionLayer(final Object layer) {
        if (layer instanceof ChunkSectionLayer chunkLayer) {
            return chunkLayer;
        }
        if (layer == net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()) {
            return ChunkSectionLayer.SOLID;
        }
        if (layer == net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()) {
            return ChunkSectionLayer.CUTOUT;
        }
        if (layer == net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock()) {
            return ChunkSectionLayer.TRANSLUCENT;
        }
        throw new IllegalArgumentException("Unsupported block render layer: " + layer);
    }

    /**
     * @return 该方块是否需要在模型 JSON 中声明 {@code force_translucent}
     */
    public static boolean requiresForceTranslucent(final Block block) {
        final NonNullSupplier<Supplier<?>> layer = get(block);
        if (layer == null) {
            return false;
        }
        try {
            return toChunkSectionLayer(layer.get().get()) == ChunkSectionLayer.TRANSLUCENT;
        } catch (final Throwable e) {
            // 非客户端环境（无 net.minecraft.client.* 类）时静默降级
            return false;
        }
    }
}
