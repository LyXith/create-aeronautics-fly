package io.github.fabricators_of_create.porting_lib.models.generators;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Porting-Lib 没有 26.3 版本，本地实现。
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

    /**
     * 模型 id 必须落在 {@code <modid>:item/<name>}：
     * {@code com.tterrag.registrate.providers.RegistrateItemModelProvider} 会据此把物品定义写进
     * {@code assets/<ns>/items/<name>.json}，模型本体则写进
     * {@code assets/<ns>/models/item/<name>.json}。
     */
    public ItemModelBuilder getBuilder(final String name) {
        final ItemModelBuilder builder = new ItemModelBuilder(modLoc("item/" + name));
        this.generatedModels.put(modLoc("item/" + name), builder);
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

        final List<CompletableFuture<?>> futures = new ArrayList<>();
        final Path assets = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
        for (final Map.Entry<Identifier, ItemModelBuilder> entry : this.generatedModels.entrySet()) {
            futures.add(DataProvider.saveStable(cache, entry.getValue().toJSON(),
                    assets.resolve("models").resolve(entry.getKey().getPath() + ".json")));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Item Models";
    }
}
