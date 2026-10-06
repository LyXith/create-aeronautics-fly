package dev.simulated_team.simulated.compat.create;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import dev.simulated_team.simulated.util.SimColors;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.List;

/**
 * Restores the item-tooltip registration formerly supplied by CreateRegistrate.
 */
public final class LegacyItemTooltips {
    private LegacyItemTooltips() {
    }

    public static void register(final AbstractRegistrate<?> registrate) {
        for (final RegistryEntry<Item, Item> entry : registrate.<Item, Item>getAll(Registries.ITEM)) {
            final Item item = entry.get();
            if (TooltipModifier.REGISTRY.get(item) != null) {
                continue;
            }

            TooltipModifier.REGISTRY.register(
                    item,
                    new LazyItemDescription(item)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
        }
    }

    private static FontHelper.Palette paletteFor(final Item item) {
        final Rarity rarity = item.getDefaultInstance().getRarity();
        if (rarity == Rarity.EPIC) {
            return new FontHelper.Palette(
                    TooltipHelper.styleFromColor(SimColors.EPIC_OURPLE),
                    TooltipHelper.styleFromColor(rarity.color()));
        }
        return FontHelper.Palette.STANDARD_CREATE;
    }

    /**
     * 26.3 把元素默认组件的绑定从 {@code Bootstrap} 推迟到了世界加载（单人是
     * {@code ReloadableServerResources}，多人是配置阶段的 {@code RegistryDataCollector}），
     * 所以客户端 entrypoint 里 {@code item.getDefaultInstance()} 会抛 "Components not bound yet"。
     *
     * <p>配色用到的 {@link Rarity} 正是这样一个组件，因此把 {@link ItemDescription.Modifier}
     * 的构造推迟到第一次展示 tooltip：{@code TooltipModifier#modify} 的 {@link Player} 参数非空
     * （Create 的 {@code ItemStackMixin} 会先判空再调用），而玩家存在就意味着世界已加载、组件已绑定。</p>
     */
    private static final class LazyItemDescription implements TooltipModifier {
        private final Item item;
        private TooltipModifier delegate;

        private LazyItemDescription(final Item item) {
            this.item = item;
        }

        @Override
        public void modify(final List<Component> tooltip, final Player player) {
            if (this.delegate == null) {
                this.delegate = new ItemDescription.Modifier(this.item, paletteFor(this.item));
            }

            this.delegate.modify(tooltip, player);
        }
    }
}
