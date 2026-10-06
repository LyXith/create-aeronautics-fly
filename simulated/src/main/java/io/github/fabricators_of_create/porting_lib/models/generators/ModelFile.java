package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;

/**
 * Porting-Lib 没有 26.3 版本，本地实现。
 *
 * <p>{@link ModelFile} 在 datagen 里表示「一个模型引用」，因此
 * {@link #toJSON()} 返回的是模型 id 字符串（供 blockstate 的
 * {@code model} 字段与 {@link ModelBuilder} 的 {@code parent} 字段使用）。
 * 完整的模型 JSON 由 {@link ModelBuilder#toJSON()} 产出。</p>
 */
public abstract class ModelFile {
    protected Identifier location;
    protected boolean validation;

    public ModelFile(final Identifier location) {
        this.location = location;
        this.validation = true;
    }

    public Identifier getLocation() {
        return this.location;
    }

    public boolean hasValidation() {
        return this.validation;
    }

    public ModelFile setValidation(final boolean validation) {
        this.validation = validation;
        return this;
    }

    public com.google.gson.JsonElement toJSON() {
        return new JsonPrimitive(this.location.toString());
    }

    @Override
    public String toString() {
        return String.valueOf(this.location);
    }

    public static class ExistingModelFile extends ModelFile {
        public ExistingModelFile(final Identifier location) {
            super(location);
        }
    }

    public static class UncheckedModelFile extends ModelFile {
        public UncheckedModelFile(final Identifier location) {
            super(location);
            this.validation = false;
        }

        public UncheckedModelFile(final String path) {
            this(Identifier.withDefaultNamespace(path));
        }
    }
}
