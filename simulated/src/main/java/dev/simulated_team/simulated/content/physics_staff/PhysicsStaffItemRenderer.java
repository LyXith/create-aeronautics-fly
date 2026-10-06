package dev.simulated_team.simulated.content.physics_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.client.render.FirstPersonItemFocus;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.index.SimRenderTypes;
import dev.simulated_team.simulated.util.SimDistUtil;
import dev.simulated_team.simulated.util.SimMathUtils;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import java.lang.Math;
import java.util.UUID;

public class PhysicsStaffItemRenderer {
    private static final FirstPersonItemFocus FIRST_PERSON_FOCUS = new FirstPersonItemFocus();

    static void captureFirstPersonFocus(final PoseStack ms, final Minecraft minecraft, final float partialTicks) {
        FIRST_PERSON_FOCUS.captureProjected(ms, minecraft, partialTicks);
    }

    static void captureFirstPersonBodyFocus(final PoseStack ms) {
        FIRST_PERSON_FOCUS.captureCameraRelativeWorld(ms);
    }

    public static Vec3 getFirstPersonFocusPos(final float pt) {
        return FIRST_PERSON_FOCUS.resolveCameraRelative(pt, false);
    }

    public static boolean isFirstPersonFocusWorldRelative() {
        return FIRST_PERSON_FOCUS.isCameraRelativeWorldSpace();
    }

}
