package com.tterrag.registrate.providers;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.SimRenderLayers;

import io.github.fabricators_of_create.porting_lib.models.generators.ModelBuilder;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.fabricators_of_create.porting_lib.models.generators.BlockStateProvider;
import io.github.fabricators_of_create.porting_lib.models.generators.MultiPartBlockStateBuilder;
import io.github.fabricators_of_create.porting_lib.models.generators.VariantBlockStateBuilder;
import net.fabricmc.api.EnvType;

import java.util.Optional;

public class RegistrateBlockstateProvider extends BlockStateProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> parent;

    public RegistrateBlockstateProvider(AbstractRegistrate<?> parent, PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, parent.getModid(), exFileHelper);
        this.parent = parent;
    }

    @Override
    public EnvType getSide() {
        return EnvType.CLIENT;
    }

    /**
     * 26.3 中区块渲染层由模型纹理的透明度推导（{@code FaceBakery#computeMaterialTransparency}）。
     * 当 {@link com.tterrag.registrate.builders.BlockBuilder#addLayer} 请求了半透明渲染层时，
     * 这里把默认的 {@code cube_all} 模型生成为带 {@code force_translucent} 的版本。
     */
    @Override
    public void simpleBlock(final Block block) {
        if (SimRenderLayers.requiresForceTranslucent(block)) {
            final String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            final ModelBuilder model = models().withExistingParent(name, "block/cube_all")
                    .texture("all", blockTexture(block));
            model.forceTranslucent("all");
            super.simpleBlock(block, model);
            return;
        }
        super.simpleBlock(block);
    }

    @Override
    protected void registerStatesAndModels() {
        parent.genData(ProviderType.BLOCKSTATE, this);
    }

    @Override
    public String getName() {
        return "Blockstates";
    }

    ExistingFileHelper getExistingFileHelper() {
        return this.models().existingFileHelper;
    }

    @SuppressWarnings("null")
    public Optional<VariantBlockStateBuilder> getExistingVariantBuilder(Block block) {
        return Optional.ofNullable(registeredBlocks.get(block))
                .filter(b -> b instanceof VariantBlockStateBuilder)
                .map(b -> (VariantBlockStateBuilder) b);
    }

    @SuppressWarnings("null")
    public Optional<MultiPartBlockStateBuilder> getExistingMultipartBuilder(Block block) {
        return Optional.ofNullable(registeredBlocks.get(block))
                .filter(b -> b instanceof MultiPartBlockStateBuilder)
                .map(b -> (MultiPartBlockStateBuilder) b);
    }
}
