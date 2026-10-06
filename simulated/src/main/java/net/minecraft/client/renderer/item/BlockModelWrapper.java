package net.minecraft.client.renderer.item;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3fc;

import java.util.List;

/**
 * 26.3 移除了 {@code BlockModelWrapper#computeExtents}。桩实现返回空包围盒。
 */
public final class BlockModelWrapper {
    private BlockModelWrapper() {
    }

    public static Vector3fc[] computeExtents(final List<BakedQuad> quads) {
        return new Vector3fc[]{};
    }
}
