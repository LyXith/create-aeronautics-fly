package io.github.fabricators_of_create.porting_lib.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 26.3 起 {@link Holder} 被声明为 sealed，只允许 {@code Holder$Direct} 与
 * {@code Holder$Reference} 两个实现，因此这里不再自己实现 Holder，而是直接继承
 * {@link Holder.Reference}（stand-alone、未绑定）：键在构造时确定，值在首次
 * {@link #value()} 时从 {@link BuiltInRegistries} 解析。
 */
public class DeferredHolder<R, T extends R> extends Holder.Reference<R> implements Supplier<T> {
    public static <R, T extends R> DeferredHolder<R, T> create(
            final ResourceKey<? extends Registry<R>> registryKey, final Identifier valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    public static <R, T extends R> DeferredHolder<R, T> create(
            final Identifier registryName, final Identifier valueName) {
        return create(ResourceKey.createRegistryKey(registryName), valueName);
    }

    public static <R, T extends R> DeferredHolder<R, T> create(final ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    protected final ResourceKey<R> key;
    @Nullable
    private Holder<R> holder;

    protected DeferredHolder(final ResourceKey<R> key) {
        super(Holder.Reference.Type.STAND_ALONE, ownerFor(key), key, null);
        this.key = Objects.requireNonNull(key);
        bind(false);
    }

    @SuppressWarnings("unchecked")
    private static <R> HolderOwner<R> ownerFor(final ResourceKey<R> key) {
        final Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
        if (registry != null) {
            return registry;
        }
        // 注册表尚未建立（自定义注册表）时，这个 holder 不参与序列化，
        // 因此用一个允许序列化的空 HolderOwner 占位。
        return new HolderOwner<R>() {};
    }

    @Override
    @SuppressWarnings("unchecked")
    public T value() {
        bind(true);
        if (holder == null) {
            throw new NullPointerException("Trying to access unbound value: " + key);
        }
        return (T) holder.value();
    }

    @Override
    public T get() {
        return value();
    }

    public Optional<T> asOptional() {
        return isBound() ? Optional.of(value()) : Optional.empty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected Registry<R> getRegistry() {
        return (Registry<R>) BuiltInRegistries.REGISTRY.getValue(key.registry());
    }

    protected final void bind(final boolean throwOnMissingRegistry) {
        if (holder != null) {
            return;
        }
        final Registry<R> registry = getRegistry();
        if (registry != null) {
            holder = registry.get(key).orElse(null);
        } else if (throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + this + ": " + key.registry());
        }
    }

    public Identifier getId() {
        return key.identifier();
    }

    public ResourceKey<R> getKey() {
        return key;
    }

    @Override
    public boolean isBound() {
        bind(false);
        return holder != null && holder.isBound();
    }

    @Override
    public boolean is(final Identifier id) {
        return id.equals(key.identifier());
    }

    @Override
    public boolean is(final ResourceKey<R> otherKey) {
        return key.equals(otherKey);
    }

    @Override
    public boolean is(final Predicate<ResourceKey<R>> predicate) {
        return predicate.test(key);
    }

    @Override
    public boolean is(final TagKey<R> tag) {
        bind(false);
        return holder != null && holder.is(tag);
    }

    @Override
    public boolean is(final Holder<R> other) {
        bind(false);
        return holder != null && holder.is(other);
    }

    @Override
    public Stream<TagKey<R>> tags() {
        bind(false);
        return holder == null ? Stream.empty() : holder.tags();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return Either.left(key);
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean areComponentsBound() {
        bind(false);
        return holder != null && holder.areComponentsBound();
    }

    @Override
    public net.minecraft.core.component.DataComponentMap components() {
        bind(false);
        return holder != null ? holder.components() : net.minecraft.core.component.DataComponentMap.EMPTY;
    }

    @Override
    public boolean canSerializeIn(final HolderOwner<R> owner) {
        bind(false);
        return holder != null && holder.canSerializeIn(owner);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof Holder<?> other
                && other.kind() == Kind.REFERENCE
                && other.unwrapKey().map(key::equals).orElse(false);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "DeferredHolder{%s}", key);
    }
}
