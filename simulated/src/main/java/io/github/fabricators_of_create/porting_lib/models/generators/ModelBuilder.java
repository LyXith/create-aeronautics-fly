package io.github.fabricators_of_create.porting_lib.models.generators;

import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
 */
public class ModelBuilder extends ModelFile {
    protected ModelFile parent;
    protected final Map<String, Identifier> textures = new LinkedHashMap<>();
    protected boolean loaded;
    /**
     * 26.3 中区块渲染层由纹理透明度推导，半透明方块的模型需要写成
     * {@code {"sprite": "...", "force_translucent": true}}。
     */
    protected final Set<String> translucentTextures = new LinkedHashSet<>();

    public ModelBuilder(final Identifier location) {
        super(location);
    }

    public ModelFile getParent() {
        return this.parent;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelBuilder> T parent(final ModelFile parent) {
        this.parent = parent;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelBuilder> T texture(final String key, final Identifier texture) {
        this.textures.put(key, texture);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelBuilder> T texture(final String key, final String texture) {
        return this.texture(key, Identifier.tryParse(texture));
    }

    public Map<String, Identifier> getTextures() {
        return this.textures;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelBuilder> T forceTranslucent(final String... keys) {
        this.translucentTextures.addAll(Arrays.asList(keys));
        return (T) this;
    }

    @Override
    public com.google.gson.JsonObject toJSON() {
        final com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        if (this.parent != null) {
            json.add("parent", this.parent.toJSON());
        }
        if (!this.textures.isEmpty()) {
            final com.google.gson.JsonObject textures = new com.google.gson.JsonObject();
            this.textures.forEach((key, value) -> {
                if (this.translucentTextures.contains(key)) {
                    final com.google.gson.JsonObject entry = new com.google.gson.JsonObject();
                    entry.addProperty("sprite", value.toString());
                    entry.addProperty("force_translucent", true);
                    textures.add(key, entry);
                } else {
                    textures.addProperty(key, value.toString());
                }
            });
            json.add("textures", textures);
        }
        return json;
    }
}
