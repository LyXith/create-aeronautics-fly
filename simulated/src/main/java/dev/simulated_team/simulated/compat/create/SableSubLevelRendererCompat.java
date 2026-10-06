package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Registers explicitly selected legacy block-entity renderers with Sable
 * without creating a compile-time dependency on Sable's newer client API.
 *
 * <p>The callback hands the renderer a {@link SubmitNodeCollector}, the 26.3
 * replacement for the 1.21 {@code SubmitNodeCollector}; legacy renderers that still
 * draw through a {@code SubmitNodeCollector} are adapted through
 * {@link RenderBridge} so their geometry reaches the submit-node pass.</p>
 */
public final class SableSubLevelRendererCompat {
    private SableSubLevelRendererCompat() {
    }

    public static void registerLegacyRenderers(final Logger logger, final BlockEntityType<?>... blockEntityTypes) {
        try {
            final Class<?> registry = Class.forName("dev.ryanhcode.sable.api.client.SubLevelBlockEntityRenderRegistry");
            final Class<?> rendererType = Class.forName("dev.ryanhcode.sable.api.client.SubLevelBlockEntityRenderRegistry$Renderer");
            final Method register = registry.getMethod("register", BlockEntityType.class, rendererType);

            for (final BlockEntityType<?> blockEntityType : blockEntityTypes) {
                registerLegacyRenderer(register, rendererType, blockEntityType);
            }
        } catch (final ReflectiveOperationException e) {
            logger.debug("Sable immediate block-entity renderer hook is unavailable", e);
        }
    }

    private static void registerLegacyRenderer(final Method register, final Class<?> rendererType,
                                               final BlockEntityType<?> blockEntityType)
            throws ReflectiveOperationException {
        final Object proxy = Proxy.newProxyInstance(
                SableSubLevelRendererCompat.class.getClassLoader(),
                new Class<?>[]{rendererType},
                (ignoredProxy, method, args) -> {
                    if ("render".equals(method.getName()) && args != null && args.length == 6) {
                        renderRegisteredLegacyRenderer(
                                (BlockEntity) args[0],
                                (float) args[1],
                                (PoseStack) args[2],
                                (SubmitNodeCollector) args[3],
                                (int) args[4],
                                (int) args[5]
                        );
                    }
                    return null;
                }
        );
        register.invoke(null, blockEntityType, proxy);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderRegisteredLegacyRenderer(final BlockEntity blockEntity, final float partialTick,
                                                       final PoseStack poseStack, final SubmitNodeCollector collector,
                                                       final int light, final int overlay) {
        final Object registeredRenderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher()
                .getRenderer(blockEntity);
        if (registeredRenderer instanceof final SmartBlockEntityRenderer<?> renderer) {
            RenderBridge.submit(poseStack, collector, (legacyPose, buffers) ->
                    ((SmartBlockEntityRenderer) renderer).renderExplicitlyInSubLevel(
                            blockEntity, partialTick, legacyPose, buffers, light, overlay));
        }
    }
}
