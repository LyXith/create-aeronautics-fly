package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
 */
public class MultiPartBlockStateBuilder implements IGeneratedBlockState {
    private final List<PartBuilder> parts = new ArrayList<>();

    public PartBuilder part() {
        final PartBuilder builder = new PartBuilder(this);
        this.parts.add(builder);
        return builder;
    }

    @Override
    public JsonElement toJSON() {
        final JsonArray array = new JsonArray();
        for (final PartBuilder part : this.parts) {
            array.add(part.toJSON());
        }
        return array;
    }

    public JsonElement toJson() {
        return this.toJSON();
    }

    public static class PartBuilder {
        private final MultiPartBlockStateBuilder parent;
        private final JsonObject json = new JsonObject();

        PartBuilder(final MultiPartBlockStateBuilder parent) {
            this.parent = parent;
        }

        public ConfiguredModel.Builder<PartBuilder> modelFile(final ModelFile file) {
            return ConfiguredModel.builder(this).modelFile(file);
        }

        public <T extends Comparable<T>> PartBuilder condition(final Property<T> property, final T value) {
            return this;
        }

        public <T extends Comparable<T>> PartBuilder condition(final Property<T> property, final String value) {
            return this;
        }

        public PartBuilder end() {
            return this.parent;
        }

        JsonElement toJSON() {
            return this.json;
        }
    }
}
