package com.tterrag.registrate.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * 26.3 版 Fabric API 用 {@link FluidRenderingRegistry} + {@link FluidModel.Unbaked} 取代了
 * {@code FluidRenderHandlerRegistry}，并移除了 {@code SimpleFluidRenderHandler}。
 * <p>
 * 本类在真实 {@link FluidRenderHandler} 接口上重建了等价行为：仍然/流动纹理与染色被放进
 * {@link FluidModel.Unbaked}（决定区块层与纹理），渲染回调使用接口默认实现。
 */
public class SimpleFluidRenderHandler implements FluidRenderHandler {
    private final Identifier stillTexture;
    private final Identifier flowingTexture;
    private final Identifier tintedTexture;
    private final int tint;
    private final boolean translucent;

    public SimpleFluidRenderHandler(final Identifier stillTexture, final Identifier flowingTexture) {
        this(stillTexture, flowingTexture, flowingTexture, -1, false);
    }

    public SimpleFluidRenderHandler(final Identifier stillTexture, final Identifier flowingTexture,
                                    final Identifier tintedTexture, final int tint) {
        this(stillTexture, flowingTexture, tintedTexture, tint, false);
    }

    public SimpleFluidRenderHandler(final Identifier stillTexture, final Identifier flowingTexture,
                                    final Identifier tintedTexture, final int tint, final boolean translucent) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintedTexture = tintedTexture;
        this.tint = tint;
        this.translucent = translucent;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    public Identifier getStillTexture() {
        return this.stillTexture;
    }

    public Identifier getFlowingTexture() {
        return this.flowingTexture;
    }

    public Identifier getTintedTexture() {
        return this.tintedTexture;
    }

    /**
     * @return {@code -1} 表示不染色，否则为 RGB 颜色
     */
    public int getTintColor() {
        return this.tint;
    }

    public FluidModel.Unbaked createFluidModel() {
        final BlockTintSource tintSource = BlockTintSources.constant(this.tint < 0 ? 0xFFFFFF : this.tint);
        return new FluidModel.Unbaked(
                new Material(this.stillTexture, this.translucent),
                new Material(this.flowingTexture, this.translucent),
                new Material(this.tintedTexture, this.translucent),
                tintSource);
    }

    /**
     * 将本处理器注册到 {@code still}/{@code flowing} 两个流体上。
     */
    public void register(final net.minecraft.world.level.material.Fluid still,
                         @Nullable final net.minecraft.world.level.material.Fluid flowing) {
        final FluidRenderHandler handler = this;
        if (flowing == null || flowing == still) {
            FluidRenderingRegistry.register(still, this.createFluidModel(), handler);
        } else {
            FluidRenderingRegistry.register(still, flowing, this.createFluidModel(), handler);
        }
    }
}
