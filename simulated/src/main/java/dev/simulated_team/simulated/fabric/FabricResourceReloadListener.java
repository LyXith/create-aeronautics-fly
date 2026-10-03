package dev.simulated_team.simulated.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record FabricResourceReloadListener(
        Identifier id,
        PreparableReloadListener delegate
) implements IdentifiableResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return this.id;
    }

    @Override
    public CompletableFuture<Void> reload(final SharedState sharedState, final Executor preparationExecutor,
                                          final PreparationBarrier preparationBarrier, final Executor applicationExecutor) {
        return this.delegate.reload(sharedState, preparationExecutor, preparationBarrier, applicationExecutor);
    }
}
