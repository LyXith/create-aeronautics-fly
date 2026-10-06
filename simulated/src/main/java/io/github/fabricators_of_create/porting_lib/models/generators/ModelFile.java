package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
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

    public JsonObject toJSON() {
        final JsonObject json = new JsonObject();
        json.addProperty("model", this.location.toString());
        return json;
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
