package dev.eriksonn.aeronautics.fabric.data;

import com.mojang.serialization.Lifecycle;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.index.AeroAdvancements;
import dev.eriksonn.aeronautics.index.AeroSoundEvents;
import dev.eriksonn.aeronautics.index.AeroTags;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class AeronauticsDataGenerator implements DataGeneratorEntrypoint {
    private static final ResourceKey<JukeboxSong> CLOUD_SKIPPER_SONG =
            ResourceKey.create(Registries.JUKEBOX_SONG, Aeronautics.path("cloud_skipper"));

    /**
     * jukebox_song 是 datapack 动态注册表：运行时从
     * {@code data/aeronautics/jukebox_song/cloud_skipper.json} 读，datagen 里没有 datapack，
     * 而 {@code music_disc_cloud_skipper} 的默认组件又必须解析这个 holder，否则
     * DataComponentInitializers 会抛 {@code Missing element aeronautics:cloud_skipper}。
     *
     * <p>不能用 {@link DataGeneratorEntrypoint#buildReloadableRegistry}：Fabric 会先给
     * jukebox_song 塞一个 stub，再 {@code add} 就会撞 {@code Multiple entries with same key}。
     * 所以自己在 Fabric 的 provider 之上合并出一份注册表。
     *
     * <p>字段必须与那个 JSON 保持一致。
     */
    private static HolderLookup.Provider withCloudSkipperSong(final HolderLookup.Provider base) {
        final MappedRegistry<JukeboxSong> songs = new MappedRegistry<>(Registries.JUKEBOX_SONG, Lifecycle.stable());

        // 基础 provider 里已经放了 vanilla 的 datapack 歌曲（minecraft:13 等），必须一起搬过来，
        // 否则 vanilla 唱片的 jukebox_playable 组件会解析失败。
        base.lookup(Registries.JUKEBOX_SONG).ifPresent(lookup -> lookup.listElements()
                .forEach(holder -> songs.register(holder.key(), holder.value(), RegistrationInfo.BUILT_IN)));

        if (songs.get(CLOUD_SKIPPER_SONG).isEmpty()) {
            songs.register(CLOUD_SKIPPER_SONG, cloudSkipperSong(), RegistrationInfo.BUILT_IN);
        }
        songs.freeze();

        return new HolderLookup.Provider() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return base.listRegistryKeys();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(final ResourceKey<? extends Registry<? extends T>> key) {
                return Registries.JUKEBOX_SONG.equals(key)
                        ? Optional.of((HolderLookup.RegistryLookup<T>) songs)
                        : base.lookup(key);
            }
        };
    }

    private static JukeboxSong cloudSkipperSong() {
        final Identifier discSound = AeroSoundEvents.MUSIC_DISC_CLOUD_SKIPPER.id();
        final SoundEvent discSoundEvent = BuiltInRegistries.SOUND_EVENT
                .getOptional(ResourceKey.create(Registries.SOUND_EVENT, discSound))
                // 声音事件通常在 mod init 时就注册好了；万一 RegistrationProvider 还没提交，
                // 就地造一个等价的 SoundEvent——这个 holder 只在 datagen 里被解析一次。
                .orElseGet(() -> SoundEvent.createVariableRangeEvent(discSound));

        return new JukeboxSong(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(discSoundEvent),
                Component.translatable("jukebox_song.aeronautics.cloud_skipper"),
                225.0f,
                12);
    }

    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator generator) {
        // 26.3 把元素的默认数据组件从 Item 构造期挪到了 DataComponentInitializers，
        // 只有 ReloadableServerResources 加载时才会对全部 holder 执行 PendingComponents#apply。
        // Fabric 的 datagen 在那之前运行，此时模组物品的 holder 还没有绑定组件，
        // 任何 new ItemStack(模组物品) 都会抛 "Components not bound yet"，所以先手动绑定一次。
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                .build(withCloudSkipperSong(generator.getRegistries().join()))
                .forEach(DataComponentInitializers.PendingComponents::apply);

        // 必须在 setupDatagen 之前注册，否则 root generator 已构造会直接抛异常。
        AeroTags.addGenerators();

        final FabricDataGenerator.Pack pack = generator.createPack();
        Aeronautics.getRegistrate().setupDatagen(
                pack, new ExistingFileHelper(List.of(), Set.of(), false, null, null));
        pack.addProvider(AeroAdvancements::new);
        pack.addProvider((output, registries) -> AeroSoundEvents.REGISTRY.getProvider(output));
    }
}
