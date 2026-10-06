package foundry.veil.api.client.render.shader.uniform;

/**
 * Write access to a single shader uniform.
 *
 * <p>In 1.21.1 these values were pushed straight into an OpenGL program with
 * {@code glUniform*}. Minecraft 26.3 renders through renderpearl, where every
 * uniform lives inside a {@code layout(std140) uniform} block uploaded as one
 * {@link com.mojang.renderpearl.api.buffers.GpuBuffer}. Implementations therefore
 * stage the value here and the owner uploads the whole block when the render pass
 * is recorded.
 */
public interface ShaderUniformAccess {

    void setFloat(float value);

    void setVector(float x, float y);

    void setVector(float x, float y, float z, float w);
}
