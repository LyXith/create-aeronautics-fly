package com.tterrag.registrate.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.tterrag.registrate.AbstractRegistrate;

import lombok.Getter;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootSubProvider;
import net.minecraft.core.HolderLookup;

public class RegistrateEntityLootTables extends FabricEntityLootSubProvider implements RegistrateLootTables {

    private final AbstractRegistrate<?> parent;
    @Getter
    private final HolderLookup.Provider provider;
    private final Consumer<RegistrateEntityLootTables> callback;

    public RegistrateEntityLootTables(HolderLookup.Provider provider, AbstractRegistrate<?> parent, Consumer<RegistrateEntityLootTables> callback, FabricPackOutput output) {
        super(output, CompletableFuture.completedFuture(provider));
        this.parent = parent;
        this.provider = provider;
        this.callback = callback;
    }

    @Override
    public void generate() {
        callback.accept(this);
    }

    @Override
    public String getName() {
        return "Registrate Entity Loot Tables";
    }
}
