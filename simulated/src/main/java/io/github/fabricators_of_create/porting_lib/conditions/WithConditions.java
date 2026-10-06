package io.github.fabricators_of_create.porting_lib.conditions;

import java.util.List;

/** Porting-Lib 没有 26.3 版本，本地桩实现。 */
public record WithConditions<T>(List<ICondition> conditions, T value) {
    public static <T> WithConditions<T> of(final T value) {
        return new WithConditions<>(List.of(), value);
    }
}
