package io.github.fabricators_of_create.porting_lib.models.generators;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

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

    /**
     * 26.3 的 {@link Identifier#withDefaultNamespace} 会把整个字符串当路径，遇到
     * {@code ns:path} 里的冒号会直接抛异常；而 datagen 的 parent 传递的常常就是完整 id。
     */
    public Identifier mcLoc(final String path) {
        return path.indexOf(':') >= 0 ? Identifier.parse(path) : Identifier.withDefaultNamespace(path);
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        registerModels();

        final List<CompletableFuture<?>> futures = new ArrayList<>();
        // PathProvider 负责插入 <namespace>，直接拼 getOutputFolder() 会漏掉它
        final PackOutput.PathProvider models = this.output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        for (final Map.Entry<Identifier, ItemModelBuilder> entry : this.generatedModels.entrySet()) {
            futures.add(DataProvider.saveStable(cache, entry.getValue().toJSON(), models.json(entry.getKey())));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Item Models";
    }
}
