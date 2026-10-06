package dev.simulated_team.simulated.index;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.Identifier;

public final class SimRenderTypes {

    /**
     * Forces this class's static initializers to run.
     *
     * <p>{@link net.minecraft.client.renderer.RenderPipelines#register} only inserts into a
     * map, and {@code ShaderManager} reads that map exactly once — during the first resource
     * reload. A pipeline registered afterwards is never handed to the compiler, so
     * {@code PipelineCache} has no entry for it and every draw using it is dropped silently.
     * Call this from the client entrypoint, before that reload happens.
     */
    public static void init() {
    }

    // Split translucent laser composition into two commutative operations.
    // This keeps intersecting colors stable when camera-based quad order flips.
    private static final BlendFunction LASER_ATTENUATION_BLEND = new BlendFunction(
            BlendFactor.ZERO, BlendFactor.ONE_MINUS_SRC_ALPHA,
            BlendFactor.ZERO, BlendFactor.ONE);
    private static final BlendFunction LASER_EMISSION_BLEND = new BlendFunction(
            BlendFactor.SRC_ALPHA, BlendFactor.ONE,
            BlendFactor.ZERO, BlendFactor.ONE);

    // Native 1.21 core-shader port of the original Veil laser. The vertex format,
    // blend, cull, depth-write and primitive mode intentionally match upstream.
    private static final RenderPipeline LASER_PIPELINE = nativeLaserPipeline("laser", true);

    // Fabric draws this variant after translucent terrain. It still depth-tests
    // against the world, but must not put the near-camera wall into the depth
    // buffer before particles and clouds are rendered.
    private static final RenderPipeline LASER_LATE_ATTENUATION_PIPELINE =
            mixedLaserPipeline("laser_late_attenuation", LASER_ATTENUATION_BLEND);
    private static final RenderPipeline LASER_LATE_PIPELINE =
            mixedLaserPipeline("laser_late", LASER_EMISSION_BLEND);

    // A distinct copy of the proven late native pipeline for Iris. It is drawn
    // only after Iris finalizes its world composite, so Iris never substitutes a
    // shader-pack program for the laser core shader.
    private static final RenderPipeline LASER_IRIS_COMPOSITE_ATTENUATION_PIPELINE =
            mixedLaserPipeline("laser_iris_composite_attenuation", LASER_ATTENUATION_BLEND);
    private static final RenderPipeline LASER_IRIS_COMPOSITE_PIPELINE =
            mixedLaserPipeline("laser_iris_composite", LASER_EMISSION_BLEND);

    // A deliberately faint additive pass around the post-composite Iris beam.
    // Keeping it separate prevents the glow blend from changing the proven core.
    private static final RenderPipeline LASER_IRIS_GLOW_PIPELINE =
            irisLaserGlowPipeline("laser_iris_glow");

    private static final RenderPipeline BLOCK_TRANSLUCENT_PIPELINE = terrainPipeline(
            "block_translucent", DefaultVertexFormat.BLOCK, true);

    // 26.3: core/rendertype_text_see_through 不复存在，see-through 文本现在是 core/text
    // 加 shader define IS_SEE_THROUGH（原版 RenderPipelines.TEXT_SEE_THROUGH 就是
    // TEXT_SNIPPET + 这个宏）。管线没有声明 SAMPLER2，所以 IS_SEE_THROUGH 下不再读光照
    // 贴图是安全的；顶点格式里的 UV2 只是无人消费的多余属性，绑定时按名字匹配。
    private static final RenderPipeline LOCK_PIPELINE = RenderPipelines.register(RenderPipeline.builder()
            .withLocation(Simulated.path("pipeline/lock"))
            .withVertexShader("core/text")
            .withFragmentShader("core/text")
            .withShaderDefine("IS_SEE_THROUGH")
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            // 26.3: DepthTestFunction.NO_DEPTH_TEST -> CompareOp.ALWAYS_PASS, 且不写深度
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build());

    // Reuse the vanilla shader-compatible lightning pipeline, but draw to the main target.
    // RenderType.lightning() writes to the weather target, which has already been composited
    // by the time shader-pack-safe staff overlays are drawn.
    private static final RenderType STAFF_OVERLAY = create(
            "simulated_staff_overlay", RenderPipelines.LIGHTNING);
    private static final RenderType STAFF_SELECTION_EDGE = create(
            "simulated_staff_selection_edge", RenderPipelines.LIGHTNING);
    private static final RenderType STAFF_SELECTION_FACE = RenderType.create(
            "simulated_staff_selection_face", RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
                    .withTexture("Sampler0", AllSpecialTextures.CHECKERED.getLocation())
                    .useLightmap().useOverlay().createRenderSetup());
    private static final RenderType LASER = createSorted(
            "simulated_laser", LASER_PIPELINE);
    private static final RenderType LASER_LATE_ATTENUATION = create(
            "simulated_laser_late_attenuation", LASER_LATE_ATTENUATION_PIPELINE);
    private static final RenderType LASER_LATE = create(
            "simulated_laser_late", LASER_LATE_PIPELINE);
    private static final RenderType LASER_IRIS_COMPOSITE_ATTENUATION = create(
            "simulated_laser_iris_composite_attenuation", LASER_IRIS_COMPOSITE_ATTENUATION_PIPELINE);
    private static final RenderType LASER_IRIS_COMPOSITE = create(
            "simulated_laser_iris_composite", LASER_IRIS_COMPOSITE_PIPELINE);
    private static final RenderType LASER_IRIS_GLOW = create(
            "simulated_laser_iris_glow", LASER_IRIS_GLOW_PIPELINE);
    private static final RenderType LENS = net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock();

    private static final RenderType LOCK = RenderType.create(
            "simulated_lock", RenderSetup.builder(LOCK_PIPELINE)
                    .withTexture("Sampler0", Simulated.path("textures/gui/lock.png"))
                    .useLightmap().createRenderSetup());

    // Iris cannot classify mod-defined terrain pipelines. A vanilla entity
    // pipeline keeps the standalone rope texture visible in shader passes.
    private static final RenderType ROPE = net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(
            Simulated.path("textures/block/rope_particle.png"), false);

    private static final RenderType BLOCK_TRANSLUCENT = RenderType.create(
            "simulated_block_translucent", RenderSetup.builder(BLOCK_TRANSLUCENT_PIPELINE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    // Translucent terrain quads must be back-to-front before upload.
                    .useLightmap().sortOnUpload().createRenderSetup());

    /**
     * Shader-pack-safe layer for textured placement previews.
     *
     * <p>The moving-block shader discards vertex alpha in some packs, while the
     * entity translucent shader can treat a world-space ghost as a refractive
     * entity. The translucent particle pipeline accepts the attributes emitted
     * by baked block models, preserves vertex color/alpha, and can still sample
     * the block atlas.</p>
     */
    private static final RenderType GHOST_BLOCK = RenderType.create(
            "simulated_ghost_block", RenderSetup.builder(RenderPipelines.TRANSLUCENT_PARTICLE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .useLightmap().createRenderSetup());

    private SimRenderTypes() {
    }

    private static RenderPipeline nativeLaserPipeline(final String name, final boolean depthWrite) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader(Simulated.path("core/laser"))
                .withFragmentShader(Simulated.path("core/laser"))
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, depthWrite))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build());
    }

    private static RenderPipeline irisLaserGlowPipeline(final String name) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader(Simulated.path("core/laser"))
                .withFragmentShader(Simulated.path("core/laser"))
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build());
    }

    private static RenderPipeline mixedLaserPipeline(final String name, final BlendFunction blend) {
        return RenderPipelines.register(RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader(Simulated.path("core/laser"))
                .withFragmentShader(Simulated.path("core/laser"))
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withColorTargetState(new ColorTargetState(blend))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .build());
    }

    private static RenderPipeline terrainPipeline(final String name, final VertexFormat format,
                                                  final boolean translucent) {
        final RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(Simulated.path("pipeline/" + name))
                .withVertexShader("core/block")
                .withFragmentShader("core/block")
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withBindGroupLayout(BindGroupLayouts.LIGHTING)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
                .withShaderDefine("ALPHA_CUTOUT", 0.1f)
                .withVertexBinding(0, format)
                .withPrimitiveTopology(PrimitiveTopology.QUADS);
        if (translucent) {
            builder.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false));
        }
        return RenderPipelines.register(builder.build());
    }

    private static RenderType create(final String name, final RenderPipeline pipeline) {
        return RenderType.create(name, RenderSetup.builder(pipeline).createRenderSetup());
    }

    private static RenderType createSorted(final String name, final RenderPipeline pipeline) {
        // The original Veil render type sorts its translucent quads on upload.
        return RenderType.create(name, RenderSetup.builder(pipeline).sortOnUpload().createRenderSetup());
    }

    public static RenderType staffOverlay() {
        return STAFF_OVERLAY;
    }

    public static RenderType staffSelectionEdge() {
        return STAFF_SELECTION_EDGE;
    }

    public static RenderType staffSelectionFace() {
        return STAFF_SELECTION_FACE;
    }

    public static RenderType laser() {
        return LASER;
    }

    public static RenderType lateLaser() {
        return LASER_LATE;
    }

    public static RenderType lateLaserAttenuation() {
        return LASER_LATE_ATTENUATION;
    }

    public static RenderType irisCompositeLaser() {
        return LASER_IRIS_COMPOSITE;
    }

    public static RenderType irisCompositeLaserAttenuation() {
        return LASER_IRIS_COMPOSITE_ATTENUATION;
    }

    public static RenderType irisCompositeLaserGlow() {
        return LASER_IRIS_GLOW;
    }

    public static RenderType lens() {
        return LENS;
    }

    public static RenderType lock() {
        return LOCK;
    }

    public static RenderType rope() {
        return ROPE;
    }

    public static RenderType blockTranslucent() {
        return BLOCK_TRANSLUCENT;
    }

    public static RenderType ghostBlock() {
        return GHOST_BLOCK;
    }

    public static RenderType itemGlowingSolid(boolean shadersActive) {
        return shadersActive ? Sheets.cutoutBlockItemSheet() : CreateRenderTypes.itemGlowingSolid();
    }

    public static RenderType itemGlowingTranslucent(boolean shadersActive) {
        return shadersActive ? Sheets.translucentBlockItemSheet() : CreateRenderTypes.itemGlowingTranslucent();
    }

    public static RenderType spring(final Identifier texture) {
        // Use a vanilla pipeline so Iris can map the render type into shader packs.
        // The old Veil spring shader is not available in the 1.21.10 Fabric port.
        // SpringRenderer emits a second, inward-facing copy of the mesh itself;
        // disabling culling would draw both copies on top of each other and z-fight.
        return net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(texture);
    }
}
