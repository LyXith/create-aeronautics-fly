package io.github.fabricators_of_create.porting_lib.data;

import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现（项目中仅作类型引用）。
 */
public abstract class LanguageProvider {
    protected final PackOutput output;
    protected final String locale;

    protected LanguageProvider(final PackOutput output, final String locale,
                               final CompletableFuture<?> registries) {
        this.output = output;
        this.locale = locale;
    }

    protected abstract void addTranslations(final Consumer<String> builder);
}
