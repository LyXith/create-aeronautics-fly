package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.service.SimEntityDataSerialization;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class FabricSimEntityDataSerialization implements SimEntityDataSerialization {
    @Override
    public <A, T extends EntityDataSerializer<A>> void registerDataSerializer(final String name, final T serializer) {
        FabricEntityDataRegistry.register(Simulated.path(name), serializer);
    }
}
