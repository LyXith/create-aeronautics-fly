package dev.simulated_team.simulated.mixin.end_sea;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.simulated_team.simulated.content.end_sea.EndSeaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    /**
     * 26.3 把 {@code renderLevel} 拆成了 {@code render}：矩阵参数被收进 {@link CameraRenderState}，
     * {@code DeltaTracker} 也不再出现在签名里。
     */
    @Inject(method = "render", at = @At("RETURN"))
    public void render(final GraphicsResourceAllocator resourceAllocator, final boolean renderOutline,
                       final CameraRenderState cameraState, final GpuBufferSlice terrainFog, final Vector4f fogColor,
                       final boolean shouldRenderSky, final boolean consistentDepthRequired, final CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        EndSeaRenderer.render(minecraft.gameRenderer.mainCamera(), minecraft.gameRenderer);
    }

}
