package foundry.veil.api.client.render.post;

import net.minecraft.resources.Identifier;
import com.mojang.renderpearl.api.pipeline.BlendFunction;

import java.util.List;
import java.util.Map;

/**
 * Declares one post-processing effect: where it reads from, where it writes to and
 * which uniforms/samplers its GLSL declares.
 *
 * <p>This replaces Veil's {@code assets/<mod>/pinwheel/post/*.json} descriptors. The
 * 26.3 equivalent of that JSON is this record: the shader itself still lives at
 * {@code assets/<mod>/shaders/post/<name>.fsh}, only the wiring is described here
 * because Minecraft has no registry for third-party post chains.
 *
 * @param location       id used to look the effect up, e.g. {@code simulated:diagram}
 * @param inputFramebuffer  framebuffer id the colour sampler reads
 * @param outputFramebuffer framebuffer id that receives the result
 * @param colorSampler   GLSL sampler bound to the input's colour attachment
 * @param depthSampler   GLSL sampler bound to the input's depth attachment, or {@code null}
 * @param uniformBlocks  std140 uniform blocks declared by both shader stages
 * @param textures       extra sampler name to texture location bindings
 * @param blend          how the result is merged into the output target
 */
public record PostPipelineSpec(
        Identifier location,
        Identifier inputFramebuffer,
        Identifier outputFramebuffer,
        String colorSampler,
        String depthSampler,
        List<String> uniformBlocks,
        Map<String, Identifier> textures,
        BlendFunction blend) {

    public PostPipelineSpec {
        uniformBlocks = List.copyOf(uniformBlocks);
        textures = Map.copyOf(textures);
    }
}
