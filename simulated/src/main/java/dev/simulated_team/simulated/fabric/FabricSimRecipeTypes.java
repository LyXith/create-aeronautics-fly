package dev.simulated_team.simulated.fabric;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.fabric.data.PortableEngineDyeingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

public final class FabricSimRecipeTypes {
    private static final PortableEngineDyeingRecipe RECIPE = new PortableEngineDyeingRecipe();

    /**
     * 26.3 移除了 {@code CustomRecipe.Serializer}：无字段的自定义配方直接用
     * {@code MapCodec#unit} / {@code StreamCodec#unit} 包住一个单例。
     */
    public static final RecipeSerializer<PortableEngineDyeingRecipe> PORTABLE_ENGINE_DYEING =
            new RecipeSerializer<>(MapCodec.unit(RECIPE), StreamCodec.unit(RECIPE));

    private FabricSimRecipeTypes() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Simulated.path("portable_engine_dyeing"), PORTABLE_ENGINE_DYEING);
    }
}
