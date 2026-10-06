package com.tterrag.registrate.fabric;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.fabricmc.api.EnvType;
import net.minecraft.resources.Identifier;

public record FluidData(String translationKey, int light) {

    public interface RenderHandlerFactory {
        SimpleFluidRenderHandler create(Identifier stillTexture, Identifier flowingTexture);
    }

    public static RenderHandlerFactory createDefaultHandler() {
        return SimpleFluidRenderHandler::new;
    }

    public static <T extends SimpleFlowableFluid> void registerRenderHandler(NonNullSupplier<RenderHandlerFactory> renderHandler, T entry, Identifier stillTexture, Identifier flowingTexture) {
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () ->
                renderHandler.get().create(stillTexture, flowingTexture).register(entry.getSource(), entry));
    }
}
