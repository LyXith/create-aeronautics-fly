package foundry.veil.api.client.render.post;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry and runner for the mod's post-processing effects.
 *
 * <p>Veil discovered {@code pinwheel/post/*.json} during resource reload and exposed
 * {@code getPipeline}/{@code runPipeline} on top of that. Here the descriptor is
 * registered explicitly at client init instead, because 26.3 has no post-effect
 * registry a third party can contribute to.
 */
public final class PostProcessingManager {
    private final Map<Identifier, PostPipelineSpec> specs = new HashMap<>();
    private final Map<Identifier, PostPipeline> pipelines = new HashMap<>();
    private final PostPipeline.Context context = new PostPipeline.Context();

    /**
     * Registers an effect. The pipeline (and its GLSL) is built lazily the first time
     * the effect is looked up, so this may be called before resources are loaded.
     */
    public void register(final PostPipelineSpec spec) {
        this.specs.put(spec.location(), spec);
        this.pipelines.remove(spec.location());
    }

    /**
     * @return the effect registered under {@code location}, or {@code null} if nothing
     * has been registered for that id
     */
    @Nullable
    public PostPipeline getPipeline(final Identifier location) {
        final PostPipeline existing = this.pipelines.get(location);
        if (existing != null) {
            return existing;
        }

        final PostPipelineSpec spec = this.specs.get(location);
        if (spec == null) {
            return null;
        }

        final PostPipeline pipeline = new PostPipeline(spec);
        this.pipelines.put(location, pipeline);
        return pipeline;
    }

    /** The framebuffer binding table used by the next {@link #runPipeline} call. */
    public PostPipeline.Context getPostPipelineContext() {
        return this.context;
    }

    /**
     * Executes {@code pipeline} against the framebuffers bound on the shared context.
     * A {@code null} pipeline means the id was never registered, so there is nothing to run.
     */
    public void runPipeline(@Nullable final PostPipeline pipeline, final boolean clear) {
        if (pipeline == null) {
            return;
        }

        try {
            pipeline.run(this.context, clear);
        } finally {
            this.context.clearBindings();
        }
    }
}
