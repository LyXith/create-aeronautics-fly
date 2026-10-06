package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
 */
public abstract class BlockStateProvider implements DataProvider {
    protected final PackOutput output;
    protected final String modid;
    protected final ExistingFileHelper existingFileHelper;
    protected final Map<Block, Object> registeredBlocks = new HashMap<>();

    private BlockModelProvider blockModels;
    private ItemModelProvider itemModelProvider;

    public BlockStateProvider(final PackOutput output, final String modid, final ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
    }

    protected abstract void registerStatesAndModels();

    public VariantBlockStateBuilder getVariantBuilder(final Block block) {
        Object existing = this.registeredBlocks.get(block);
        if (!(existing instanceof VariantBlockStateBuilder)) {
            existing = new VariantBlockStateBuilder();
            this.registeredBlocks.put(block, existing);
        }
        return (VariantBlockStateBuilder) existing;
    }

    public MultiPartBlockStateBuilder getMultipartBuilder(final Block block) {
        Object existing = this.registeredBlocks.get(block);
        if (!(existing instanceof MultiPartBlockStateBuilder)) {
            existing = new MultiPartBlockStateBuilder();
            this.registeredBlocks.put(block, existing);
        }
        return (MultiPartBlockStateBuilder) existing;
    }

    public BlockModelProvider models() {
        if (this.blockModels == null) {
            this.blockModels = new BlockModelProvider(this.modid, this.existingFileHelper);
        }
        return this.blockModels;
    }

    public ItemModelProvider itemModels() {
        if (this.itemModelProvider == null) {
            this.itemModelProvider = new ItemModelProvider(this.output, this.modid, this.existingFileHelper);
        }
        return this.itemModelProvider;
    }

    public Identifier modLoc(final String path) {
        return Identifier.fromNamespaceAndPath(this.modid, path);
    }

    public Identifier mcLoc(final String path) {
        return Identifier.withDefaultNamespace(path);
    }

    public Identifier blockTexture(final Block block) {
        return modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath());
    }

    public ModelFile cubeAll(final Block block) {
        return models().getExistingFile(blockTexture(block));
    }

    public void simpleBlock(final Block block) {
        this.registeredBlocks.put(block, ConfiguredModel.single(cubeAll(block)));
    }

    public void simpleBlock(final Block block, final ModelFile model) {
        this.registeredBlocks.put(block, ConfiguredModel.single(model));
    }

    public void simpleBlock(final Block block, final Function<ModelFile, ConfiguredModel[]> generator) {
        this.registeredBlocks.put(block, generator.apply(cubeAll(block)));
    }

    public void simpleBlock(final Block block, final ConfiguredModel... models) {
        this.registeredBlocks.put(block, models);
    }

    public void simpleBlockItem(final Block block, final ModelFile model) {
    }

    public void simpleBlockWithItem(final Block block, final ModelFile model) {
        simpleBlock(block, model);
    }

    public void axisBlock(final net.minecraft.world.level.block.RotatedPillarBlock block) {
        simpleBlock(block);
    }

    public void axisBlock(final net.minecraft.world.level.block.RotatedPillarBlock block, final Identifier texture) {
        axisBlock(block, texture, texture);
    }

    public void axisBlock(final net.minecraft.world.level.block.RotatedPillarBlock block, final Identifier side, final Identifier end) {
    }

    public void horizontalBlock(final Block block, final ModelFile model) {
        this.registeredBlocks.put(block, ConfiguredModel.single(model));
    }

    public void horizontalBlock(final Block block, final ModelFile model, final int yRot) {
        this.registeredBlocks.put(block, ConfiguredModel.single(model, yRot));
    }

    public void horizontalBlock(final Block block, final Function<BlockState, ModelFile> generator) {
        this.registeredBlocks.put(block, ConfiguredModel.single(generator.apply(block.defaultBlockState())));
    }

    public void horizontalFaceBlock(final Block block, final ModelFile model) {
        horizontalBlock(block, model);
    }

    public void horizontalFaceBlock(final Block block, final Function<BlockState, ModelFile> generator) {
        horizontalBlock(block, generator);
    }

    public void directionalBlock(final Block block, final ModelFile model) {
        this.registeredBlocks.put(block, ConfiguredModel.single(model));
    }

    public void directionalBlock(final Block block, final Function<BlockState, ModelFile> generator) {
        this.registeredBlocks.put(block, ConfiguredModel.single(generator.apply(block.defaultBlockState())));
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        registerStatesAndModels();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Blockstates";
    }

    public static class ConfiguredModelList implements IGeneratedBlockState {
        private final ConfiguredModel[] models;

        public ConfiguredModelList(final ConfiguredModel... models) {
            this.models = models;
        }

        public ConfiguredModel[] getModels() {
            return this.models;
        }

        @Override
        public JsonElement toJSON() {
            final JsonArray array = new JsonArray();
            for (final ConfiguredModel model : this.models) {
                array.add(model.toJSON());
            }
            if (array.size() == 1) {
                return array.get(0);
            }
            return array;
        }
    }
}
