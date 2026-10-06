package com.tterrag.registrate.providers.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Function4;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLootTableSubProvider;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableProviderImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Registrate 的战利品表回调门面，构建在 26.3 + Fabric 的数据生成管线之上。
 *
 * <p>1.21 时代的 {@code LootTableProvider#run(CachedOutput)} 已被移除：vanilla 的
 * {@link net.minecraft.data.loot.LootTableProvider} 现在只是一个
 * {@link net.minecraft.core.registries.SingleRegistryBootstrap}，不再实现
 * {@link net.minecraft.data.DataProvider}。Fabric 保留了可写盘的入口
 * {@link FabricLootTableProviderImpl#run}，按 {@link ContextKeySet} 逐个运行
 * {@link FabricLootTableSubProvider}，Registrate 这里就是把每个 {@link LootType}
 * 对应的子表生成器接进该入口。</p>
 */
public class RegistrateLootTableProvider implements RegistrateProvider {

    public interface LootType<T extends RegistrateLootTables> {

        LootType<RegistrateBlockLootTables> BLOCK = register("block", net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK, RegistrateBlockLootTables::new);
        LootType<RegistrateEntityLootTables> ENTITY = register("entity", net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY, RegistrateEntityLootTables::new);

        T getLootCreator(HolderLookup.Provider provider, AbstractRegistrate<?> parent, Consumer<T> callback, FabricPackOutput output);
        ContextKeySet getLootSet();

        static <T extends RegistrateLootTables> LootType<T> register(String name, ContextKeySet set, Function4<HolderLookup.Provider, AbstractRegistrate<?>, Consumer<T>, FabricPackOutput, T> factory) {
            LootType<T> type = new LootType<T>() {
                @Override
                public T getLootCreator(HolderLookup.Provider provider, AbstractRegistrate<?> parent, Consumer<T> callback, FabricPackOutput output) {
                    return factory.apply(provider, parent, callback, output);
                }

                @Override
                public ContextKeySet getLootSet() {
                    return set;
                }
            };
            LOOT_TYPES.put(name, type);
            return type;
        }
    }

    private static final Map<String, LootType<?>> LOOT_TYPES = new HashMap<>();

    private final AbstractRegistrate<?> parent;

    private final Multimap<LootType<?>, Consumer<? super RegistrateLootTables>> specialLootActions = HashMultimap.create();
    private final FabricPackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    public RegistrateLootTableProvider(AbstractRegistrate<?> parent, FabricPackOutput output,
                                       CompletableFuture<HolderLookup.Provider> provider) {
        this.parent = parent;
        this.output = output;
        this.registriesFuture = provider;
    }

    public HolderLookup.Provider getProvider() {
        return registriesFuture.join();
    }

    public <T> Holder<T> resolve(ResourceKey<T> key) {
        return getProvider().lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    @Override
    public EnvType getSide() {
        return EnvType.SERVER;
    }

    @SuppressWarnings("unchecked")
    public <T extends RegistrateLootTables> void addLootAction(LootType<T> type, NonNullConsumer<? extends RegistrateLootTables> action) {
        this.specialLootActions.put(type, (Consumer<RegistrateLootTables>) action);
    }

    public List<LootType<?>> getLootTypes() {
        return ImmutableList.copyOf(LOOT_TYPES.values());
    }

    @SuppressWarnings("unchecked")
    private FabricLootTableSubProvider createSubProvider(HolderLookup.Provider registries, LootType<?> type) {
        return (FabricLootTableSubProvider) type.getLootCreator(registries, parent,
                callback -> specialLootActions.get(type).forEach(action -> action.accept(callback)), output);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        parent.genData(ProviderType.LOOT, this);

        return registriesFuture.thenCompose(registries -> {
            final List<CompletableFuture<?>> futures = new ArrayList<>();
            for (final LootType<?> type : LOOT_TYPES.values()) {
                final FabricLootTableSubProvider subProvider = createSubProvider(registries, type);
                futures.add(FabricLootTableProviderImpl.run(cache, subProvider, type.getLootSet(), output, registriesFuture));
            }
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Registrate Loot Tables";
    }
}
