package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
 */
public class ConfiguredModel implements IGeneratedBlockState {
    public ModelFile model;
    public int x;
    public int y;
    public boolean uvLock;
    public int weight = 1;

    public ConfiguredModel(final ModelFile model) {
        this.model = model;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>(null, null);
    }

    public static <T> Builder<T> builder(final T owner) {
        return new Builder<>(owner, null);
    }

    static <T> Builder<T> builder(final T owner, final java.util.function.Consumer<ConfiguredModel> onAddModel) {
        return new Builder<>(owner, onAddModel);
    }

    public static ConfiguredModel[] single(final ModelFile model) {
        return new ConfiguredModel[]{new ConfiguredModel(model)};
    }

    public static ConfiguredModel[] single(final ModelFile model, final int yRot) {
        final ConfiguredModel configured = new ConfiguredModel(model);
        configured.y = yRot;
        return new ConfiguredModel[]{configured};
    }

    public static ConfiguredModel[] empty() {
        return new ConfiguredModel[0];
    }

    public static ConfiguredModel[] rotate(final ConfiguredModel[] models, final int yRot) {
        for (final ConfiguredModel model : models) {
            model.y = (model.y + yRot) % 360;
        }
        return models;
    }

    public static ConfiguredModel[] horizontal(final Direction direction, final ConfiguredModel... models) {
        return rotate(models, (int) direction.toYRot());
    }

    @Override
    public JsonElement toJSON() {
        final JsonObject json = new JsonObject();
        if (this.model != null && this.model.getLocation() != null) {
            json.addProperty("model", this.model.getLocation().toString());
        }
        if (this.x != 0) {
            json.addProperty("x", this.x);
        }
        if (this.y != 0) {
            json.addProperty("y", this.y);
        }
        if (this.uvLock) {
            json.addProperty("uvlock", true);
        }
        if (this.weight != 1) {
            json.addProperty("weight", this.weight);
        }
        return json;
    }

    public static class Builder<T> {
        private final T owner;
        private final ConfiguredModel model;
        private final java.util.function.Consumer<ConfiguredModel> onAddModel;

        Builder(final T owner, final java.util.function.Consumer<ConfiguredModel> onAddModel) {
            this.owner = owner;
            this.model = new ConfiguredModel(null);
            this.onAddModel = onAddModel;
        }

        public Builder<T> modelFile(final ModelFile file) {
            this.model.model = file;
            return this;
        }

        public Builder<T> modelFile(final String path) {
            this.model.model = new ModelFile.UncheckedModelFile(path);
            return this;
        }

        public Builder<T> rotation(final int rot) {
            this.model.y = rot % 360;
            return this;
        }

        public Builder<T> rotationX(final int rot) {
            this.model.x = rot % 360;
            return this;
        }

        public Builder<T> rotationY(final int rot) {
            this.model.y = rot % 360;
            return this;
        }

        public Builder<T> uvLock(final boolean uvLock) {
            this.model.uvLock = uvLock;
            return this;
        }

        public Builder<T> weight(final int weight) {
            this.model.weight = weight;
            return this;
        }

        /**
         * 把当前累积的 {@link ConfiguredModel} 交给 owner（多重方块状态的 PartBuilder）
         * 记录下来，再返回 owner 以便继续链式写 condition。
         */
        public T addModel() {
            if (this.onAddModel != null) {
                this.onAddModel.accept(this.model);
            }
            return this.owner;
        }

        public ConfiguredModel build() {
            return this.model;
        }
    }
}
