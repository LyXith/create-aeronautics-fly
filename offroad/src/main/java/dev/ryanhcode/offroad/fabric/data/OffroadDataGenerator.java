package dev.ryanhcode.offroad.fabric.data;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.data.OffroadTags;
import dev.ryanhcode.offroad.index.OffroadAdvancements;
import dev.ryanhcode.offroad.index.OffroadSoundEvents;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Set;

public final class OffroadDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator generator) {
        // 与 SimulatedDataGenerator 相同：26.3 的默认数据组件要到 ReloadableServerResources
        // 才会被绑定，而 Fabric 的 datagen 更早运行，此时 new ItemStack(模组物品) 会抛
        // "Components not bound yet"，所以先手动绑定一次。
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                .build(generator.getRegistries().join())
                .forEach(DataComponentInitializers.PendingComponents::apply);

        // 只注册 data.OffroadTags：ProviderType.BLOCK_TAGS 用 Map.put 覆盖，
        // index.OffroadTags 里那版是空实现，先调它会把 borehead_* 标签挤掉。
        OffroadTags.addGenerators();

        final FabricDataGenerator.Pack pack = generator.createPack();
        Offroad.getRegistrate().setupDatagen(
                pack, new ExistingFileHelper(List.of(), Set.of(), false, null, null));
        pack.addProvider(OffroadAdvancements::new);
        pack.addProvider((output, registries) -> OffroadSoundEvents.REGISTRY.getProvider(output));
    }
}
