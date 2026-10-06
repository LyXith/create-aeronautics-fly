package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Porting-Lib 没有 26.3 版本，本地实现。
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
            // 调用方普遍写成 ``....end().part();``，链尾会多挂一个还没填模型的空 part，
            // 空 part 序列化出来是非法的 `"apply": []`，直接跳过。
            if (part.models.isEmpty()) {
                continue;
            }
            array.add(part.toJSON());
        }
        return array;
    }

    public JsonElement toJson() {
        return this.toJSON();
    }

    public static class PartBuilder {
        private final MultiPartBlockStateBuilder parent;
        private final JsonArray models = new JsonArray();
        private final Map<Property<?>, List<Comparable<?>>> conditions = new LinkedHashMap<>();

        PartBuilder(final MultiPartBlockStateBuilder parent) {
            this.parent = parent;
        }

        public ConfiguredModel.Builder<PartBuilder> modelFile(final ModelFile file) {
            return ConfiguredModel.builder(this, this::addModel).modelFile(file);
        }

        public PartBuilder addModel(final ConfiguredModel model) {
            this.models.add(model.toJSON());
            return this;
        }

        public <T extends Comparable<T>> PartBuilder condition(final Property<T> property, final T value) {
            this.conditions.computeIfAbsent(property, p -> new ArrayList<>()).add(value);
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> PartBuilder condition(final Property<T> property, final T... values) {
            for (final T value : values) {
                condition(property, value);
            }
            return this;
        }

        /** 26.3 中 {@link Property#getValue(String)} 返回 {@link java.util.Optional}。 */
        public <T extends Comparable<T>> PartBuilder condition(final Property<T> property, final String value) {
            return property.getValue(value)
                    .<PartBuilder>map(v -> condition(property, v))
                    .orElse(this);
        }

        /** 26.3 版 porting-lib 的 chain 以 {@code end()} 收尾回到多重方块状态构建器。 */
        public MultiPartBlockStateBuilder end() {
            return this.parent;
        }

        JsonElement toJSON() {
            final JsonObject json = new JsonObject();
            json.add("apply", this.models.size() == 1 ? this.models.get(0) : this.models);
            if (!this.conditions.isEmpty()) {
                final Map<String, JsonElement> when = new TreeMap<>();
                for (final Map.Entry<Property<?>, List<Comparable<?>>> entry : this.conditions.entrySet()) {
                    final Property<?> property = entry.getKey();
                    final List<Comparable<?>> values = entry.getValue();
                    // 同一属性的多个取值在 multipart 里是 “或”，写成 "a|b"
                    final StringBuilder joined = new StringBuilder();
                    for (final Comparable<?> value : values) {
                        if (joined.length() > 0) {
                            joined.append('|');
                        }
                        joined.append(render(property, value));
                    }
                    when.put(property.getName(), new JsonPrimitive(joined.toString()));
                }
                final JsonObject whenJson = new JsonObject();
                when.forEach(whenJson::add);
                json.add("when", whenJson);
            }
            return json;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static String render(final Property property, final Comparable value) {
            return property.getName(value);
        }
    }
}
