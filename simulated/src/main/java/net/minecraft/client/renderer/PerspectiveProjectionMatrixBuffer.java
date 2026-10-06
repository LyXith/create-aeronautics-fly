package net.minecraft.client.renderer;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import org.joml.Matrix4f;

/**
 * 26.3 移除了 {@code PerspectiveProjectionMatrixBuffer}。桩实现不生成投影 UBO。
 */
public class PerspectiveProjectionMatrixBuffer {
    private final String name;

    public PerspectiveProjectionMatrixBuffer(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public GpuBufferSlice getBuffer(final Matrix4f projection) {
        return null;
    }
}
