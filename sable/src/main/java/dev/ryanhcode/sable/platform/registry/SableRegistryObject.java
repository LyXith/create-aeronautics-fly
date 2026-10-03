package dev.ryanhcode.sable.platform.registry;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

/**
 * Minimal replacement for Veil's {@code foundry.veil.platform.registry.RegistryObject}
 * on the mc26.1 port branch (Veil has no 26.1 build). Values are registered
 * eagerly, so this is a plain holder.
 */
public final class SableRegistryObject<T> implements Supplier<T> {

    private final Identifier id;
    private final T value;

    SableRegistryObject(final Identifier id, final T value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public T get() {
        return this.value;
    }

    public Identifier getId() {
        return this.id;
    }
}
