package dev.simulated_team.simulated.data.advancements;

import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * 26.3 把进阶触发器改成了「codec + 玩家已登记条件」模型：
 * {@link net.minecraft.advancements.triggers.CriterionTrigger} 只负责 {@code codec()} 与
 * {@code createCriterion(...)}，分发由 {@link SimpleCriterionTrigger#trigger} 通过
 * {@code PlayerAdvancements#getTriggerMapForType} 完成，因此不再需要手工维护
 * {@code addPlayerListener}/{@code removePlayerListeners} 监听器表。
 *
 * <p>与旧实现不同，触发条件（{@link Instance}）是从 JSON 里解码出来的，判定发生在
 * {@link SimpleCriterionTrigger#trigger} 的谓词里，而不是运行时传入的 supplier 列表。</p>
 */
@ParametersAreNonnullByDefault
public abstract class SimulatedCriterionTriggerBase<T extends SimulatedCriterionTriggerBase.Instance>
        extends SimpleCriterionTrigger<T> {

    private final Identifier id;

    protected SimulatedCriterionTriggerBase(final Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return this.id;
    }

    /** 触发器条件本身：进阶 JSON 里 {@code conditions} 的载体。 */
    public static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        private final Identifier id;

        public Instance(final Identifier id) {
            this.id = id;
        }

        public Identifier getId() {
            return this.id;
        }

        @Override
        public Optional<Holder<LootItemCondition>> player() {
            return Optional.empty();
        }
    }
}
