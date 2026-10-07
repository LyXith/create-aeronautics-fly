package dev.simulated_team.simulated.mixin.rope;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.client.rope.BaseRope;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientLevelRopeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

/**
 * sable 在客户端自带一套绳子（{@code RopeManager} 静态表 + {@code BaseRope}），而我们的
 * {@code ServerRopeStrand extends RopePhysicsObject} 正是 sable 的绳子物理对象，于是
 * {@code SubLevelPhysicsSystem} 会把<b>我们的</b>绳子原样同步进 sable 的表里，产生两个后果：
 *
 * <ol>
 *   <li>{@code RopeManager.renderAll -> BaseRope.render} 把我们的绳子当方块再画一遍，和
 *       {@code RopeStrandRenderer} 画的真绳子叠在一起；</li>
 *   <li>{@code MinecraftMixin.sable$attackRope} 用 {@code RopeManager.pick} 按 UUID 拾取到
 *       <b>我们的</b>绳子，随后发 sable 自己的 {@code ServerboundRemoveRopePacket} 并
 *       {@code setReturnValue(false)} 吃掉左键攻击 —— 服务端只删物理对象、不清方块状态，
 *       我们自己的 {@code RopeBreakPacket} 永远发不出去。</li>
 * </ol>
 *
 * <p>这里只针对"归我们所有的"绳子做手术（用 {@code ClientLevelRopeManager} 判定归属），
 * 因此 sable 自己 spawn 的绳子仍然按原样渲染/拾取：
 * <ul>
 *   <li>{@code render} 直接取消 —— 绘制完全交给 {@code RopeStrandRenderer}；</li>
 *   <li>{@code getPoints} 返回空表 —— {@code pick} 的逐段循环自然跳过它，
 *       于是 sable 永远选不中我们的绳子，其打绳逻辑形同虚设。</li>
 * </ul>
 */
@Mixin(value = BaseRope.class, remap = false)
public abstract class BaseRopeMixin {

    @Shadow
    @Final
    protected UUID id;

    @Unique
    private boolean simulated$belongsToStrand() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return false;
        }
        return ClientLevelRopeManager.getOrCreate(minecraft.level).getStrand(this.id) != null;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void simulated$skipOurRopeRender(final PoseStack poseStack, final SubmitNodeCollector collector,
                                             final Vec3 cameraPos, final CallbackInfo ci) {
        if (this.simulated$belongsToStrand()) {
            ci.cancel();
        }
    }

    @Inject(method = "getPoints", at = @At("HEAD"), cancellable = true)
    private void simulated$hideOurRopeFromPick(final CallbackInfoReturnable<List<Vec3>> cir) {
        if (this.simulated$belongsToStrand()) {
            cir.setReturnValue(List.of());
        }
    }
}
