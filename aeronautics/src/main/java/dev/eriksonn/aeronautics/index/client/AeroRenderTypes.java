package dev.eriksonn.aeronautics.index.client;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.eriksonn.aeronautics.Aeronautics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.renderer.BindGroupLayouts;

public final class AeroRenderTypes {
    public static final Identifier LEVITITE_SHADER = Aeronautics.path("levitite/levitite");
    private static final Identifier FIRE_PALETTE =
            Aeronautics.path("textures/effects/fire_palette.png");

    private static final RenderPipeline BURNER_FLAME_PIPELINE =
            burnerFlamePipeline("burner_flame");

    // Iris composites its shader-pack targets after normal block-entity
    // rendering. Drawing a custom core shader into those targets can make the
    // otherwise opaque flame inherit the pack's transparency. This copy is
    // used only after Iris has completed that composite.
    private static final RenderPipeline BURNER_FLAME_IRIS_COMPOSITE_PIPELINE =
            burnerFlamePipeline("burner_flame_iris_composite");

    private static final RenderType BURNER_FLAME = RenderType.create(
            "aeronautics_burner_flame",
            burnerFlameState(BURNER_FLAME_PIPELINE)
    );

    private static final RenderType BURNER_FLAME_IRIS_COMPOSITE = RenderType.create(
            "aeronautics_burner_flame_iris_composite",
            burnerFlameState(BURNER_FLAME_IRIS_COMPOSITE_PIPELINE)
    );

    private AeroRenderTypes() {
    }

    private static RenderPipeline burnerFlamePipeline(final String name) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Aeronautics.path("pipeline/" + name))
                .withVertexShader(Aeronautics.path("core/burner_flame"))
                .withFragmentShader(Aeronautics.path("core/burner_flame"))
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withCull(false)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build());
    }

    private static RenderSetup burnerFlameState(final RenderPipeline pipeline) {
        return RenderSetup.builder(pipeline)
                .withTexture("Sampler0", FIRE_PALETTE)
                .createRenderSetup();
    }

    public static void init() {
        // Force pipeline registration before Minecraft's first shader reload.
    }

    public static RenderType burnerFlame() {
        return BURNER_FLAME;
    }

    public static RenderType irisCompositeBurnerFlame() {
        return BURNER_FLAME_IRIS_COMPOSITE;
    }

    public static RenderType levitite() {
        return RenderTypes.translucentMovingBlock();
    }

    public static RenderType levititeGhosts() {
        return RenderTypes.translucentMovingBlock();
    }
}
