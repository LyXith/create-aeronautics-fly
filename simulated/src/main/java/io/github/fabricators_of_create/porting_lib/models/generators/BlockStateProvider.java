package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        return ((VariantBlockStateBuilder) existing).setBlock(block);
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
        itemModels().getBuilder(BuiltInRegistries.BLOCK.getKey(block).getPath()).parent(model);
    }

    public void simpleBlockWithItem(final Block block, final ModelFile model) {
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    public void axisBlock(final net.minecraft.world.level.block.RotatedPillarBlock block) {
        simpleBlock(block);
    }

    public void axisBlock(final net.minecraft.world.level.block.RotatedPillarBlock block, final Identifier texture) {
        axisBlock(block, texture, texture);
    }

    public void axisBlock(final net.minecraft.world.level.block.RotatedPillarBlock block, final Identifier side, final Identifier end) {
        final String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        final ModelBuilder model = models()
                .withExistingParent(name, "block/cube_column")
                .texture("side", side)
                .texture("end", end);
        this.registeredBlocks.put(block, ConfiguredModel.single(model));
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

        final List<CompletableFuture<?>> futures = new ArrayList<>();
        final Path assets = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);

        for (final Map.Entry<Block, Object> entry : this.registeredBlocks.entrySet()) {
            final JsonElement json = toBlockstateJson(entry.getValue());
            if (json == null) {
                continue;
            }
            final Identifier id = BuiltInRegistries.BLOCK.getKey(entry.getKey());
            futures.add(DataProvider.saveStable(cache, json,
                    assets.resolve("blockstates").resolve(id.getPath() + ".json")));
        }

        for (final Map.Entry<Identifier, ModelBuilder> entry : this.models().generatedModels.entrySet()) {
            futures.add(DataProvider.saveStable(cache, entry.getValue().toJSON(), modelFile(assets, entry.getKey())));
        }

        for (final Map.Entry<Identifier, ItemModelBuilder> entry : this.itemModels().generatedModels.entrySet()) {
            futures.add(DataProvider.saveStable(cache, entry.getValue().toJSON(), modelFile(assets, entry.getKey())));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /** 模型 id 路径已含 {@code block/} / {@code item/}，因此直接拼到 {@code models/} 之后。 */
    private static Path modelFile(final Path assets, final Identifier modelId) {
        return assets.resolve("models").resolve(modelId.getPath() + ".json");
    }

    private static JsonElement toBlockstateJson(final Object value) {
        if (value instanceof VariantBlockStateBuilder builder) {
            final JsonObject variants = new JsonObject();
            builder.getModels().forEach((state, models) -> variants.add(state.toString(), models.toJSON()));
            final JsonObject json = new JsonObject();
            json.add("variants", variants);
            return json;
        }
        if (value instanceof MultiPartBlockStateBuilder builder) {
            final JsonObject json = new JsonObject();
            json.add("multipart", builder.toJSON());
            return json;
        }
        if (value instanceof ConfiguredModel[] models) {
            final JsonObject variants = new JsonObject();
            variants.add("", new ConfiguredModelList(models).toJSON());
            final JsonObject json = new JsonObject();
            json.add("variants", variants);
            return json;
        }
        return null;
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
