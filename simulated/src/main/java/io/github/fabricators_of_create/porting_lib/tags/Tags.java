package io.github.fabricators_of_create.porting_lib.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现（仅保留项目实际用到的约定标签）。
 */
public final class Tags {
    private Tags() {
    }

    public static final class Items {
        private Items() {
        }

        public static final TagKey<Item> RODS_WOODEN = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("rods/wooden"));

        private static TagKey<Item> item(final String path) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
        }
    }
}
