package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

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

    /**
     * 26.3 的 {@link Identifier#withDefaultNamespace} 会把整个字符串当路径，遇到
     * {@code ns:path} 里的冒号会直接抛异常；而 datagen 的 parent 传递的常常就是完整 id。
     */
    public Identifier mcLoc(final String path) {
        return path.indexOf(':') >= 0 ? Identifier.parse(path) : Identifier.withDefaultNamespace(path);
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
        horizontalBlock(block, state -> model, 0);
    }

    public void horizontalBlock(final Block block, final ModelFile model, final int yRot) {
        horizontalBlock(block, state -> model, yRot);
    }

    public void horizontalBlock(final Block block, final Function<BlockState, ModelFile> generator) {
        horizontalBlock(block, generator, 0);
    }

    private void horizontalBlock(final Block block, final Function<BlockState, ModelFile> generator, final int extraYRot) {
        final EnumProperty<Direction> facing = facingProperty(block);
        getVariantBuilder(block).forAllStates(state -> {
            final ConfiguredModel configured = new ConfiguredModel(generator.apply(state));
            int y = 0;
            if (facing != null && state.hasProperty(facing)) {
                y = yRot(state.getValue(facing));
            }
            configured.y = (y + extraYRot) % 360;
            return new ConfiguredModel[]{configured};
        });
    }

    /**
     * 贴在地面/墙面/天花板上的方块：{@code floor} 不旋转，{@code wall} 绕 X 转 90°，
     * {@code ceiling} 绕 X 转 180° 并把 y 再补 180°。
     */
    public void horizontalFaceBlock(final Block block, final ModelFile model) {
        horizontalFaceBlock(block, state -> model);
    }

    public void horizontalFaceBlock(final Block block, final Function<BlockState, ModelFile> generator) {
        final EnumProperty<Direction> facing = facingProperty(block);
        final Property<?> face = block.getStateDefinition().getProperty("face");
        getVariantBuilder(block).forAllStates(state -> {
            final ConfiguredModel configured = new ConfiguredModel(generator.apply(state));
            final int facingY = facing != null && state.hasProperty(facing) ? yRot(state.getValue(facing)) : 0;
            int x = 0;
            int y = facingY;
            if (face != null && state.hasProperty(face)) {
                switch (renderValue(face, state.getValue(face))) {
                    case "ceiling" -> {
                        x = 180;
                        y = (facingY + 180) % 360;
                    }
                    case "wall" -> x = 90;
                    default -> { }
                }
            }
            configured.x = x;
            configured.y = y;
            return new ConfiguredModel[]{configured};
        });
    }

    /** 六向方块：up/down 只绕 X，四个水平方向绕 X 90° 再按 facing 转 y。 */
    public void directionalBlock(final Block block, final ModelFile model) {
        directionalBlock(block, state -> model);
    }

    public void directionalBlock(final Block block, final Function<BlockState, ModelFile> generator) {
        final EnumProperty<Direction> facing = facingProperty(block);
        getVariantBuilder(block).forAllStates(state -> {
            final ConfiguredModel configured = new ConfiguredModel(generator.apply(state));
            if (facing != null && state.hasProperty(facing)) {
                final Direction direction = state.getValue(facing);
                if (direction.getAxis().isVertical()) {
                    configured.x = direction == Direction.DOWN ? 180 : 0;
                    configured.y = 0;
                } else {
                    configured.x = 90;
                    configured.y = yRot(direction);
                }
            }
            return new ConfiguredModel[]{configured};
        });
    }

    /**
     * 26.3 里已经没有独立的 {@code DirectionProperty}，方向属性就是
     * {@code EnumProperty<Direction>}；按方块自己的 {@code facing} 属性取，
     * 这样水平四向 / 六向 / 贴面三向的方块都能拿到各自正确的取值集合。
     */
    @SuppressWarnings("unchecked")
    private static EnumProperty<Direction> facingProperty(final Block block) {
        final Property<?> property = block.getStateDefinition().getProperty("facing");
        if (property instanceof EnumProperty<?> enumProperty
                && enumProperty.getPossibleValues().contains(Direction.NORTH)) {
            return (EnumProperty<Direction>) enumProperty;
        }
        return null;
    }

    /**
     * 26.3 的 {@link Direction#toYRot()} 返回 float，且取值是
     * south=0 / west=90 / north=180 / east=270，而方块状态 JSON 的 y 旋转约定是
     * north=0 / east=90 / south=180 / west=270，正好差 180°。
     */
    private static int yRot(final Direction direction) {
        return (int) ((direction.toYRot() + 180) % 360);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String renderValue(final Property<?> property, final Comparable<?> value) {
        return ((Property) property).getName(value);
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        registerStatesAndModels();

        final List<CompletableFuture<?>> futures = new ArrayList<>();
        // PathProvider 负责插入 <namespace>，直接拼 getOutputFolder() 会漏掉它
        final PackOutput.PathProvider blockstates = this.output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        final PackOutput.PathProvider models = this.output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");

        for (final Map.Entry<Block, Object> entry : this.registeredBlocks.entrySet()) {
            final JsonElement json = toBlockstateJson(entry.getValue());
            if (json == null) {
                continue;
            }
            futures.add(DataProvider.saveStable(cache, json,
                    blockstates.json(BuiltInRegistries.BLOCK.getKey(entry.getKey()))));
        }

        for (final Map.Entry<Identifier, ModelBuilder> entry : this.models().generatedModels.entrySet()) {
            futures.add(DataProvider.saveStable(cache, entry.getValue().toJSON(), models.json(entry.getKey())));
        }

        for (final Map.Entry<Identifier, ItemModelBuilder> entry : this.itemModels().generatedModels.entrySet()) {
            futures.add(DataProvider.saveStable(cache, entry.getValue().toJSON(), models.json(entry.getKey())));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
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
