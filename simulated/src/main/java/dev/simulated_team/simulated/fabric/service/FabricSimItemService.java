package dev.simulated_team.simulated.fabric.service;

import com.zurrtum.create.AllItemTags;
import dev.simulated_team.simulated.service.SimItemService;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 26.3 移除了 {@code FuelValues}（以及 {@code MinecraftServer#fuelValues()}）：
 * 熔炉燃料现在由物品数据组件 {@code minecraft:cooking_fuel} 提供，
 * 其燃烧时间是 {@link ResolvableInt}，可能引用动态注册表
 * {@code minecraft:context_int_provider}，因此需要一个 {@link LootContext} 才能解析
 * （原版见 {@code AbstractFurnaceBlockEntity#getBurnDuration}）。
 *
 * <p>本类在服务器启动后缓存一个基于 overworld 的 {@link LootContext}；
 * 单人游戏客户端（例如 Ponder 场景）则回退到集成服务器。</p>
 */
public final class FabricSimItemService implements SimItemService {

    private static volatile @Nullable MinecraftServer server;
    private static volatile @Nullable LootContext cachedContext;

    // 必须是 public 无参构造：ServiceLoader 通过它实例化本类（private 会直接 ServiceConfigurationError）
    public FabricSimItemService() {}

    public static void setServer(final @Nullable MinecraftServer value) {
        server = value;
        cachedContext = null;
    }

    @Override
    public int getBurnTime(final ItemStack stack) {
        if (stack.isEmpty()) return 0;
        final CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        if (fuel == null) return 0;

        final ResolvableInt burnTime = fuel.burnTime();
        if (burnTime instanceof ResolvableInt.Constant constant) {
            return constant.value();
        }

        final LootContext context = context();
        return context == null ? 0 : burnTime.get(context, 0);
    }

    @Override
    public int getSuperheatedBurnTime(final ItemStack stack) {
        return stack.typeHolder().is(AllItemTags.BLAZE_BURNER_FUEL_SPECIAL) ? 3200 : 0;
    }

    private static @Nullable LootContext context() {
        final LootContext existing = cachedContext;
        if (existing != null) return existing;

        final ServerLevel level = resolveLevel();
        if (level == null) return null;

        final LootContext created = new LootContext.Builder(
                new LootParams.Builder(level).create(LootContextParamSets.EMPTY))
                .create(Optional.empty());
        cachedContext = created;
        return created;
    }

    private static @Nullable ServerLevel resolveLevel() {
        final MinecraftServer running = server;
        if (running != null) return running.overworld();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            final net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft != null) {
                final MinecraftServer integrated = minecraft.getSingleplayerServer();
                if (integrated != null) return integrated.overworld();
            }
        }
        return null;
    }
}
