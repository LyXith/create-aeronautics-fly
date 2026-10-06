package io.github.fabricators_of_create.porting_lib.models.generators;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
 */
public class BlockModelProvider {
    public final ExistingFileHelper existingFileHelper;
    protected final String modid;
    protected final Map<Identifier, ModelBuilder> generatedModels = new LinkedHashMap<>();

    protected BlockModelProvider(final String modid, final ExistingFileHelper existingFileHelper) {
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
    }

    public ModelBuilder getBuilder(final String name) {
        final ModelBuilder builder = new ModelBuilder(modLoc("block/" + name));
        this.generatedModels.put(modLoc("block/" + name), builder);
        return builder;
    }

    public ModelFile getExistingFile(final Identifier loc) {
        return new ModelFile.ExistingModelFile(loc);
    }

    public ModelBuilder withExistingParent(final String name, final String parent) {
        return getBuilder(name).parent(new ModelFile.ExistingModelFile(mcLoc(parent)));
    }

    public ModelBuilder withExistingParent(final String name, final Identifier parent) {
        return getBuilder(name).parent(new ModelFile.ExistingModelFile(parent));
    }

    public Identifier modLoc(final String path) {
        return Identifier.fromNamespaceAndPath(this.modid, path);
    }

    public Identifier mcLoc(final String path) {
        return Identifier.withDefaultNamespace(path);
    }

    public Identifier blockTexture(final net.minecraft.world.level.block.Block block) {
        return modLoc("block/" + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath());
    }
}
