package io.github.fabricators_of_create.porting_lib.models.generators;

import com.google.gson.JsonElement;

/** Porting-Lib 没有 26.3 版本，本地桩实现。 */
public interface IGeneratedBlockState {
    JsonElement toJSON();
}
