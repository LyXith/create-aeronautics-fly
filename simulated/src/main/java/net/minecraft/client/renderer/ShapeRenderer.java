package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 26.3 移除了 {@code ShapeRenderer}。桩实现不写入任何几何。
 */
public final class ShapeRenderer {
    private ShapeRenderer() {
    }

    public static void renderShape(final PoseStack poseStack, final VertexConsumer consumer, final VoxelShape shape,
                                   final double x, final double y, final double z, final int color, final float lineWidth) {
    }

    public static void renderLine(final PoseStack poseStack, final VertexConsumer consumer,
                                  final double x1, final double y1, final double z1,
                                  final double x2, final double y2, final double z2, final int color) {
    }

    public static void renderSolidBox(final PoseStack poseStack, final VertexConsumer consumer,
                                      final float minX, final float minY, final float minZ,
                                      final float maxX, final float maxY, final float maxZ, final int color) {
    }
}
