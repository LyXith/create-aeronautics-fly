package dev.simulated_team.simulated.data.advancements;

import com.google.common.collect.Maps;
import org.jspecify.annotations.NullMarked;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@NullMarked
public abstract class SimulatedCriterionTriggerBase<T extends SimulatedCriterionTriggerBase.Instance> implements CriterionTrigger<T> {

    private final Identifier id;
    protected final Map<PlayerAdvancements, Set<Listener<T>>> listeners = Maps.newHashMap();

    public SimulatedCriterionTriggerBase(final Identifier id) {
        this.id = id;
    }

    @Override
    public void addPlayerListener(final PlayerAdvancements pPlayerAdvancements, final Listener<T> pListener) {
        final Set<Listener<T>> playerListeners = this.listeners.computeIfAbsent(pPlayerAdvancements, k -> new HashSet<>());
        playerListeners.add(pListener);
    }

    @Override
    public void removePlayerListener(final PlayerAdvancements pPlayerAdvancements, final Listener<T> pListener) {
        final Set<Listener<T>> playerListeners = this.listeners.get(pPlayerAdvancements);
        if(playerListeners != null)  {
            playerListeners.remove(pListener);
            if(playerListeners.isEmpty()) {
                this.listeners.remove(pPlayerAdvancements);
            }
        }
    }

    @Override
    public void removePlayerListeners(final PlayerAdvancements pPlayerAdvancements) {
        this.listeners.remove(pPlayerAdvancements);
    }

    public Identifier getId() {
        return this.id;
    }

    protected void trigger(final ServerPlayer player, @Nullable final List<Supplier<Object>> suppliers) {
        final PlayerAdvancements playerAdvancements = player.getAdvancements();
        final Set<Listener<T>> playerListeners = this.listeners.get(playerAdvancements);
        if(playerListeners != null) {
            final List<Listener<T>> list = new LinkedList<>();

            for (final Listener<T> listener : playerListeners) {
                if(listener.trigger().test(suppliers)) {
                    list.add(listener);
                }
            }

            list.forEach(listener -> listener.run(playerAdvancements));
        }
    }

    public abstract static class Instance implements CriterionTriggerInstance {
        private final Identifier id;
        public Instance(final Identifier id) {
            this.id = id;
        }
        public Identifier getId() {
            return this.id;
        }
        protected abstract boolean test (@Nullable List<Supplier<Object>> suppliers);
    }
}