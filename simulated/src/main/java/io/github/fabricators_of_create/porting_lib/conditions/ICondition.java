package io.github.fabricators_of_create.porting_lib.conditions;

import com.google.gson.JsonObject;
import net.minecraft.util.context.ContextMap;

/** Porting-Lib 没有 26.3 版本，本地桩实现。 */
public interface ICondition {
    boolean test(ContextMap context);

    String getType();

    JsonObject toJson();
}
