package dev.simulated_team.simulated.content.items.plunger_launcher;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.equipment.zapper.ShootableGadgetRenderHandler;
import com.zurrtum.create.foundation.item.render.CustomRenderedItemModel;
import com.zurrtum.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.zurrtum.create.foundation.item.render.PartialItemModelRenderer;
import com.zurrtum.create.infrastructure.particle.AirParticleData;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.client.render.FirstPersonItemFocus;
import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerEntity;
import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerEntityRenderer;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.mixin_interface.PlayerLaunchedPlungerExtension;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

public class PlungerLauncherItemRenderer extends CustomRenderedItemModelRenderer {

    private static final FirstPersonItemFocus FIRST_PERSON_FOCUS = new FirstPersonItemFocus();
    private static final Map<AbstractClientPlayer, EnumMap<HumanoidArm, Vec3>> THIRD_PERSON_FOCUS = new WeakHashMap<>();

    static void captureFirstPersonFocus(final PoseStack matrices, final Minecraft minecraft,
                                        final float partialTicks) {
        FIRST_PERSON_FOCUS.captureProjected(matrices, minecraft, partialTicks);
    }

    static void captureFirstPersonBodyFocus(final PoseStack matrices) {
        FIRST_PERSON_FOCUS.captureCameraRelativeWorld(matrices);
    }

    static void captureThirdPersonFocus(final AbstractClientPlayer player, final ItemDisplayContext displayContext,
                                        final PoseStack matrices, final float partialTicks) {
        final Vector3f point = matrices.last().pose().transformPosition(new Vector3f());
        final Vec3 worldPoint = new Vec3(point.x, point.y, point.z)
                .add(Minecraft.getInstance().gameRenderer.getMainCamera().position());
        final HumanoidArm arm = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        // Keep an offset from this player so movement between render submissions does not leave a stale world point.
        THIRD_PERSON_FOCUS.computeIfAbsent(player, ignored -> new EnumMap<>(HumanoidArm.class))
                .put(arm, worldPoint.subtract(player.getPosition(partialTicks)));
    }

    public static @Nullable Vec3 getThirdPersonFocusPos(final AbstractClientPlayer player, final float partialTicks) {
        final var hands = THIRD_PERSON_FOCUS.get(player);
        if (hands == null) return null;
        final boolean mainHand = player.getMainHandItem().getItem() instanceof PlungerLauncherItem
                || !(player.getOffhandItem().getItem() instanceof PlungerLauncherItem);
        final HumanoidArm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        final Vec3 offset = hands.get(arm);
        return offset == null ? null : player.getPosition(partialTicks).add(offset);
    }

    public static Vec3 getFirstPersonFocusPos(final float partialTicks,
                                              final boolean rotateProjectedForShader) {
        return FIRST_PERSON_FOCUS.resolveCameraRelative(partialTicks, rotateProjectedForShader)
                .add(Minecraft.getInstance().gameRenderer.getMainCamera().position());
    }

    @Override
    protected void render(final ItemStack stack, final CustomRenderedItemModel model, final PartialItemModelRenderer renderer, final ItemDisplayContext transformType, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        ms.scale(0.8f, 0.8f, 0.8f);
        ms.translate(0, 0, 0.15f);
        renderer.render(model.getOriginalModel(), light);

        final LocalPlayer player = Minecraft.getInstance().player;
        final DeltaTracker timer = Minecraft.getInstance().getDeltaTracker();
        final float partialTicks = timer.getGameTimeDeltaPartialTick(false);

        final PlayerLaunchedPlungerExtension duck = (PlayerLaunchedPlungerExtension) player;

        final LaunchedPlungerEntity plunger = duck.simulated$getLaunchedPlunger();
        if (player.getCooldowns().getCooldownPercent(stack, partialTicks) <= 0.6f || (plunger != null && plunger.getOther() == null)) {
            if ((plunger == null || plunger.isRemoved() || plunger.getOther() != null) && player.getCooldowns().getCooldownPercent(stack, partialTicks) <= 0.4f) {
                this.renderPlunger(ms, buffer, light, true);
            }

            this.renderPlunger(ms, buffer, light, false);
        }

        ms.translate(2 / 16f, -1 / 16f, -5 / 16f);
        ms.translate(0, 0, 3 / 16f);

        if (transformType.firstPerson()) {
            captureFirstPersonFocus(ms, Minecraft.getInstance(), partialTicks);
        }
    }

    private void renderPlunger(final PoseStack ms, final MultiBufferSource buffer, final int light, final boolean first) {
        ms.pushPose();
        final SuperByteBuffer body = CachedBuffers.partial(SimPartialModels.LAUNCHED_PLUNGER_BODY, Blocks.AIR.defaultBlockState());
        final SuperByteBuffer spool = CachedBuffers.partial(SimPartialModels.LAUNCHED_PLUNGER_SPOOL, Blocks.AIR.defaultBlockState());
        final SuperByteBuffer joint = CachedBuffers.partial(SimPartialModels.LAUNCHED_PLUNGER_JOINT, Blocks.AIR.defaultBlockState());

        ms.translate(2 / 16f * (first ? -1 : 1), -1 / 16f, -5 / 16f);

        final DeltaTracker timer = Minecraft.getInstance().getDeltaTracker();
        final float partialTicks = timer.getGameTimeDeltaPartialTick(false);

        final ItemCooldowns cooldowns = Minecraft.getInstance().player.getCooldowns();
        final float cooldown = cooldowns.getCooldownPercent(new ItemStack(SimItems.PLUNGER_LAUNCHER.asItem()), partialTicks);
        if (cooldown > 0 && PlungerLauncherItem.reloadCooldown) {
            if (!first) {
                float slideIn = Mth.clamp(Mth.map(cooldown, 0.3f, 0.6f, 0, 1), 0, 1);
                slideIn = (float) Math.pow(slideIn,3);
                ms.translate(0, 0, -slideIn / 12f);
            } else {
                float slideIn = Mth.clamp(Mth.map(cooldown, 0.1f, 0.4f, 0, 1), 0, 1);
                slideIn = (float) Math.pow(slideIn,3);
                ms.translate(0, 0, -slideIn / 12f);
            }
        }

        body.light(light).renderInto(ms.last(), buffer.getBuffer(RenderTypes.solidMovingBlock()));
        joint.light(light).renderInto(ms.last(), buffer.getBuffer(RenderTypes.solidMovingBlock()));

        ms.translate(0, 0, 3 / 16f);
        spool.light(light).renderInto(ms.last(), buffer.getBuffer(RenderTypes.solidMovingBlock()));
        ms.popPose();
    }

    public static class RenderHandler extends ShootableGadgetRenderHandler {

        public void basicShoot(final InteractionHand hand) {
            final LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                final boolean rightHand = hand == InteractionHand.MAIN_HAND ^ player.getMainArm() == HumanoidArm.LEFT;
                if (rightHand) {
                    this.rightHandAnimation = .2f;
                    this.dontReequipRight = false;
                } else {
                    this.leftHandAnimation = .2f;
                    this.dontReequipLeft = false;
                }

                final Vec3 focusPos1 = LaunchedPlungerEntityRenderer.getFirstPersonFocusPos(0f);
                for (int i = 0; i < Math.random() * 4; i++) {
                    final Vec3 m2 = VecHelper.offsetRandomly(player.getViewVector(0), player.level().random, 0.5f);
                    player.level().addParticle(new AirParticleData(1, 1 / 4f), focusPos1.x, focusPos1.y, focusPos1.z, m2.x, m2.y, m2.z);
                }

                this.playSound(hand, player.position());
            }
        }

        @Override
        public void playSound(final InteractionHand hand, final Vec3 position) {

        }

        @Override
        protected boolean appliesTo(final ItemStack stack) {
            return SimItems.PLUNGER_LAUNCHER.is(stack.getItem());
        }

        @Override
        protected void transformTool(final PoseStack ms, final float flip, final float equipProgress, final float recoil, final float pt) {
            ms.translate(flip * -.1f, 0.05f, .14f);
            TransformStack.of(ms).rotateXDegrees(recoil * 80);
        }

        @Override
        protected void transformHand(final PoseStack ms, final float flip, final float equipProgress, final float recoil, final float pt) {
            ms.scale(0, 0, 0);
        }
    }
}
