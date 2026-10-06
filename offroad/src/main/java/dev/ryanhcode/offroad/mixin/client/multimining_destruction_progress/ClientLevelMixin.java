package dev.ryanhcode.offroad.mixin.client.multimining_destruction_progress;

import com.google.common.collect.Sets;
import dev.ryanhcode.offroad.handlers.client.MultiMiningBlockDestructionProgress;
import dev.ryanhcode.offroad.handlers.client.MultiMiningClientHandler;
import dev.ryanhcode.offroad.mixin_interface.client_level.MultiMiningDestructionExtension;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.SortedSet;

/**
 * 26.3 把方块破坏进度从 {@code LevelRenderer} 整个搬进了 {@link ClientLevel}：
 * {@link ClientLevel#destroyBlockProgress(int, BlockPos, int)} 直接维护 {@code destroyingBlocks} /
 * {@code destructionProgress}，渲染端由 {@code LevelExtractor} 从 {@link ClientLevel#destructionProgress()}
 * 读取，{@code LevelRenderer} 上已经不再保留这两张表。因此多重挖掘的进度只能挂在 {@link ClientLevel} 上。
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements MultiMiningDestructionExtension {

    @Shadow
    @Final
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    /**
     * vanilla 的实现直接 {@code destructionProgress.get(pos).remove(block)}，而多重挖掘的 holder 只是「容器」，
     * 自身并不在 {@code destructionProgress} 中，走 vanilla 路径会 NPE。这里改为清理 holder 持有的全部真实进度。
     */
    @Inject(method = "removeProgress", at = @At("HEAD"), cancellable = true)
    private void offroad$removeMultiMiningProgresses(final BlockDestructionProgress progress, final CallbackInfo ci) {
        if (progress instanceof final MultiMiningBlockDestructionProgress mmProgress) {
            for (final BlockDestructionProgress inner : mmProgress.otherProgresses.values()) {
                this.offroad$removeProgress(inner);
            }

            mmProgress.otherProgresses.clear();
            ci.cancel();
        }
    }

    @Override
    public void offroad$manuallyAddMultiDestructionProgress(final int id, final Map<BlockPos, MultiMiningClientHandler.ClientBlockBreakingData> clientData) {
        final BlockDestructionProgress multiMineBlockHolder = this.destroyingBlocks.computeIfAbsent(id, $ -> new MultiMiningBlockDestructionProgress(id, BlockPos.ZERO));
        if (multiMineBlockHolder instanceof final MultiMiningBlockDestructionProgress mmProgress) {

            final Map<BlockPos, BlockDestructionProgress> heldProgresses = mmProgress.otherProgresses;
            clientData.forEach((bPos, data) -> {
                final float clientDataProgress = data.destroyProgress;
                final boolean clientDataInvalid = data.invalid;
                heldProgresses.compute(bPos, (k, heldBlockBreakingProgress) -> {
                    //if we don't have a value, AND we're trying to add an invalid value, ignore
                    if (heldBlockBreakingProgress == null && (clientDataProgress == -1 || clientDataInvalid)) {
                        return null;
                    }

                    //if we have a value, but we're given an invalid progression, remove this entry
                    if (heldBlockBreakingProgress != null && (clientDataProgress == -1 || clientDataInvalid)) {
                        this.offroad$removeProgress(heldBlockBreakingProgress);
                        return null;
                    }

                    //we're given a valid progress, but we have no block progression. create a new one :>
                    if (heldBlockBreakingProgress == null) {
                        heldBlockBreakingProgress = new BlockDestructionProgress(id, bPos);
                    }

                    heldBlockBreakingProgress.setProgress((byte) clientDataProgress);
                    this.destructionProgress.computeIfAbsent(bPos.asLong(), l -> Sets.newTreeSet())
                            .add(heldBlockBreakingProgress);

                    return heldBlockBreakingProgress;
                });
            });

            // update our timer to make sure we keep voided appropriately
            mmProgress.updateTick(((LevelAccessor) (Object) this).getGameTime());
        }
    }

    @Unique
    private void offroad$removeProgress(final BlockDestructionProgress innerProgress) {
        final long progressID = innerProgress.getPos().asLong();
        final SortedSet<BlockDestructionProgress> progressSet = this.destructionProgress.get(progressID);

        // our progress set can be null
        if (progressSet != null) {
            progressSet.remove(innerProgress);

            if (progressSet.isEmpty()) {
                this.destructionProgress.remove(progressID);
            }
        }
    }
}
