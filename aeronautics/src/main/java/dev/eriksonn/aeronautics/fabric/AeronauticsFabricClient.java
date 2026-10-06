package dev.eriksonn.aeronautics.fabric;

import com.zurrtum.create.client.catnip.placement.PlacementClient;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.AeronauticsClient;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.IrisBurnerFlameRenderQueue;
import dev.eriksonn.aeronautics.events.AeronauticsClientEvents;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import foundry.veil.api.network.VeilPacketManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.tterrag.registrate.fabric.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.resources.Identifier;

import java.util.concurrent.atomic.AtomicInteger;

public final class AeronauticsFabricClient implements ClientModInitializer {
    private static final int PRODUCTION_SMOKE_REQUIRED_TICKS = 20;
    private static final Identifier LEVITITE_BLEND_STILL = Aeronautics.path("fluid/levitite_blend_still");
    private static final Identifier LEVITITE_BLEND_FLOW = Aeronautics.path("fluid/levitite_blend_flow");
    private static final AtomicInteger PRODUCTION_SMOKE_TICKS = new AtomicInteger();

    @Override
    public void onInitializeClient() {
        VeilPacketManager.registerClientReceivers();
        FabricAeroParticleTypes.registerFactories();

        // 26.3 起 Fabric 移除了 BlockRenderLayerMap：区块渲染层由模型纹理透明度推导。
        //  - 流体：FluidModel.Unbaked 的 Material.forceTranslucent（此处设为半透明）
        //  - 方块：模型 JSON 中的 force_translucent（见 assets/aeronautics/models/block/levitite*.json）
        //  - 纹理带 alpha 的方块自动落入 ChunkSectionLayer.CUTOUT（如 adjustable_burner）
        final SimpleFluidRenderHandler fluidRenderer = new SimpleFluidRenderHandler(
                LEVITITE_BLEND_STILL, LEVITITE_BLEND_FLOW, LEVITITE_BLEND_FLOW, -1, true);
        fluidRenderer.register(
                FabricAeroFluids.LEVITITE_BLEND.getSource(),
                FabricAeroFluids.LEVITITE_BLEND.get());

        AeronauticsClient.init();

        LevelRenderEvents.START_MAIN.register(
                context -> IrisBurnerFlameRenderQueue.beginWorldFrame()
        );
        LevelRenderEvents.END_MAIN.register(
                context -> IrisBurnerFlameRenderQueue.finishWorldFrameCollection()
        );
        ClientTickEvents.START_CLIENT_TICK.register(client -> AeronauticsClientEvents.clientLevelTick(false));
        ClientTickEvents.END_CLIENT_TICK.register(client -> AeronauticsClientEvents.clientLevelTick(true));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Boolean.getBoolean("aeronautics.productionSmokeTest")
                    && PRODUCTION_SMOKE_TICKS.incrementAndGet() == PRODUCTION_SMOKE_REQUIRED_TICKS) {
                // Create normally loads this only after joining a world. Exercise its mixins in production too.
                PlacementClient.tick(client);
                Aeronautics.LOGGER.info("AERONAUTICS_PRODUCTION_CLIENT_SMOKE_OK");
                client.stop();
            }
        });
    }
}
