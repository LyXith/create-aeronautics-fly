package io.github.fabricators_of_create.porting_lib.models.generators;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现。
 */
public class ItemModelProvider implements DataProvider {
    protected final PackOutput output;
    protected final String modid;
    protected final ExistingFileHelper existingFileHelper;
    protected final Map<Identifier, ItemModelBuilder> generatedModels = new LinkedHashMap<>();

    public ItemModelProvider(final PackOutput output, final String modid, final ExistingFileHelper existingFileHelper) {
        this.output = output;
        this.modid = modid;
        this.existingFileHelper = existingFileHelper;
    }

    protected void registerModels() {
    }

    public ItemModelBuilder getBuilder(final String name) {
        final ItemModelBuilder builder = new ItemModelBuilder(modLoc(name));
        this.generatedModels.put(modLoc(name), builder);
        return builder;
    }

    public ModelFile getExistingFile(final Identifier loc) {
        return new ModelFile.ExistingModelFile(loc);
    }

    public ItemModelBuilder withExistingParent(final String name, final String parent) {
        return getBuilder(name).parent(new ModelFile.ExistingModelFile(mcLoc(parent)));
    }

    public ItemModelBuilder withExistingParent(final String name, final Identifier parent) {
        return getBuilder(name).parent(new ModelFile.ExistingModelFile(parent));
    }

    public Identifier modLoc(final String path) {
        return Identifier.fromNamespaceAndPath(this.modid, path);
    }

    public Identifier mcLoc(final String path) {
        return Identifier.withDefaultNamespace(path);
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        registerModels();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Item Models";
    }
}
