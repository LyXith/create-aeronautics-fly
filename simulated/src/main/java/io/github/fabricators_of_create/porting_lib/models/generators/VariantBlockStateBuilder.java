package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Porting-Lib 没有 26.3 版本，本地实现。
 */
public class VariantBlockStateBuilder implements IGeneratedBlockState {
    private final Map<PartialBlockstate, BlockStateProvider.ConfiguredModelList> models = new LinkedHashMap<>();
    private final PartialBlockstate partialState = new PartialBlockstate();
    private Block block;

    VariantBlockStateBuilder setBlock(final Block block) {
        this.block = block;
        return this;
    }

    public VariantBlockStateBuilder forAllStates(final Function<BlockState, ConfiguredModel[]> generator) {
        if (this.block != null) {
            for (final BlockState state : this.block.getStateDefinition().getPossibleStates()) {
                final PartialBlockstate partial = new PartialBlockstate();
                for (final Property<?> property : state.getProperties()) {
                    partial.putRaw(property, state.getValue(property));
                }
                this.models.put(partial, new BlockStateProvider.ConfiguredModelList(generator.apply(state)));
            }
        }
        return this;
    }

    public Map<PartialBlockstate, BlockStateProvider.ConfiguredModelList> getModels() {
        return this.models;
    }

    public PartialBlockstate partialState() {
        return this.partialState;
    }

    public JsonElement toJSON() {
        final JsonObject json = new JsonObject();
        for (final Map.Entry<PartialBlockstate, BlockStateProvider.ConfiguredModelList> entry : this.models.entrySet()) {
            json.add(entry.getKey().toString(), entry.getValue().toJSON());
        }
        return json;
    }

    public JsonElement toJson() {
        return this.toJSON();
    }

    public static class PartialBlockstate {
        /**
         * 方块状态 JSON 的 variant key 要求按属性名排序，这里直接用
         * {@link TreeMap} 保证写入顺序稳定。
         */
        private final Map<Property<?>, Comparable<?>> properties =
                new TreeMap<>(Comparator.comparing(Property::getName));

        public <T extends Comparable<T>> PartialBlockstate withProperty(final Property<T> property, final T value) {
            this.properties.put(property, value);
            return this;
        }

        void putRaw(final Property<?> property, final Comparable<?> value) {
            this.properties.put(property, value);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PartialBlockstate other)) {
                return false;
            }
            return this.properties.equals(other.properties);
        }

        @Override
        public int hashCode() {
            return this.properties.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            for (final Map.Entry<Property<?>, Comparable<?>> entry : this.properties.entrySet()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(entry.getKey().getName()).append('=').append(render(entry.getKey(), entry.getValue()));
            }
            return sb.toString();
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static String render(final Property property, final Comparable value) {
            return property.getName(value);
        }
    }
}
