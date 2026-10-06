package dev.simulated_team.simulated.mixin.hold_interaction;

import dev.simulated_team.simulated.events.SimulatedCommonClientEvents;
import dev.simulated_team.simulated.util.SimDistUtil;
import dev.simulated_team.simulated.util.click_interactions.InteractCallback;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    // 26.3: Minecraft 没有 screen 字段了，当前屏幕改为 minecraft.gui.screen() 读取。
    // keyPress 里第一次出现该调用即原来读取 screen 的位置。
    @Inject(method = "keyPress", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0),
            cancellable = true)
    private void simulated$preOnPress(final long windowPointer, final int action, final KeyEvent event, final CallbackInfo ci) {
        if (this.minecraft.gui.screen() == null) {
            if (SimDistUtil.getClientPlayer() != null && !SimDistUtil.getClientPlayer().isSpectator()) {
                final InteractCallback.Result status = SimulatedCommonClientEvents.onBeforeMouseInput(InteractCallback.Input.key(event.key(), event.keycode()), event.modifiers(), action);
                if (status.cancelled()) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "keyPress", at = @At("TAIL"))
    private void simulated$postOnPress(final long windowPointer, final int action, final KeyEvent event, final CallbackInfo ci) {
        SimulatedCommonClientEvents.onAfterKeyPress(event.key(), event.keycode(), action, event.modifiers());
    }
}
