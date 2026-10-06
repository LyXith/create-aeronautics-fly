package dev.simulated_team.simulated.fabric.data;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.index.SimTags;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Set;

public final class SimulatedDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        // 26.3 把元素的默认数据组件从 Item 构造期挪到了 DataComponentInitializers，
        // 只有 ReloadableServerResources 加载时才会对全部 holder 执行
        // PendingComponents#apply。Fabric 的 datagen 在那之前运行，此时模组物品的
        // holder 还没有绑定组件，任何 new ItemStack(模组物品) 都会抛
        // "Components not bound yet"——所以先手动绑定一次。
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                .build(generator.getRegistries().join())
                .forEach(DataComponentInitializers.PendingComponents::apply);

        SimTags.addGenerators();
        final FabricDataGenerator.Pack pack = generator.createPack();
        Simulated.getRegistrate().setupDatagen(
                pack, new ExistingFileHelper(List.of(), Set.of(), false, null, null));
        pack.addProvider(SimAdvancements::new);
        pack.addProvider((output, registries) -> SimSoundEvents.REGISTRY.getProvider(output));
    }
}
