package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
 */
public class VariantBlockStateBuilder implements IGeneratedBlockState {
    private final Map<PartialBlockstate, BlockStateProvider.ConfiguredModelList> models = new LinkedHashMap<>();
    private final PartialBlockstate partialState = new PartialBlockstate();

    public VariantBlockStateBuilder forAllStates(final Function<BlockState, ConfiguredModel[]> generator) {
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
        @Override
        public String toString() {
            return "";
        }
    }
}
