package dev.simulated_team.simulated.data.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * 无条件触发器：任何持有该条件的玩家进度都会立刻完成。
 *
 * <p>26.3 中判定谓词的入参是解码出来的 {@link Instance}（旧版是运行时 supplier 列表），
 * 这里始终返回 {@code true}。</p>
 */
public class SimpleSimulatedTrigger extends SimulatedCriterionTriggerBase<SimulatedCriterionTriggerBase.Instance> {

    public SimpleSimulatedTrigger(final Identifier id) {
        super(id);
    }

    @Override
    public @NotNull Codec<SimulatedCriterionTriggerBase.Instance> codec() {
        return Identifier.CODEC.xmap(SimpleSimulatedTrigger.Instance::new,
                SimulatedCriterionTriggerBase.Instance::getId);
    }

    public void trigger(final ServerPlayer player) {
        super.trigger(player, instance -> true);
    }

    public Instance instance() {
        return new Instance(this.getId());
    }

    public static class Instance extends SimulatedCriterionTriggerBase.Instance {
        public Instance(final Identifier id) {
            super(id);
        }
    }
}
