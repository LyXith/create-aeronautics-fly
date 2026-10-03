package dev.simulated_team.simulated.mixin.redstone_link;

import com.zurrtum.create.client.content.redstone.link.LinkBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.compat.create.LegacyValueBoxTransform;
import dev.simulated_team.simulated.content.blocks.redstone.AbstractLinkedReceiverBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.LinkedReceiverFrequencySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LinkBehaviour.class, remap = false)
public abstract class LinkBehaviourMixin {
    @Shadow ValueBoxTransform firstSlot;
    @Shadow ValueBoxTransform secondSlot;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void simulated$receiverSlots(final SmartBlockEntity blockEntity, final CallbackInfo ci) {
        if (blockEntity instanceof AbstractLinkedReceiverBlockEntity) {
            firstSlot = LegacyValueBoxTransform.of(blockEntity, new LinkedReceiverFrequencySlot(true));
            secondSlot = LegacyValueBoxTransform.of(blockEntity, new LinkedReceiverFrequencySlot(false));
        }
    }
}
