package dev.simulated_team.simulated.network.packets;

import net.minecraft.world.phys.Vec3;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.merging_glue.MergingGlueBlock;
import dev.simulated_team.simulated.content.blocks.merging_glue.MergingGlueBlockEntity;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.service.SimConfigService;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record PlaceMergingGluePacket(BlockPos parentPos, BlockPos childPos, Direction parentFacing, Direction childFacing,
                                     InteractionHand hand) implements CustomPacketPayload {

    public static Type<PlaceMergingGluePacket> TYPE = new Type<>(Simulated.path("place_merging_glue"));

    public static StreamCodec<RegistryFriendlyByteBuf, PlaceMergingGluePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, (packet) -> packet.hand().ordinal(),
            BlockPos.STREAM_CODEC, PlaceMergingGluePacket::parentPos,
            BlockPos.STREAM_CODEC, PlaceMergingGluePacket::childPos,
            Direction.STREAM_CODEC, PlaceMergingGluePacket::parentFacing,
            Direction.STREAM_CODEC, PlaceMergingGluePacket::childFacing,
            (hand, parentPos, childPos, parentFacing, childFacing) -> new PlaceMergingGluePacket(parentPos, childPos, parentFacing, childFacing, hand == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final ServerPacketContext ctx) {
        final ServerPlayer player = ctx.player();
        final Level level = ctx.level();

        final ItemStack glue = player.getItemInHand(this.hand);
        final double distanceSquared = Sable.HELPER.distanceSquaredWithSubLevels(level, Vec3.atCenterOf(this.parentPos), Vec3.atCenterOf(this.childPos));
        final float mergingGlueRange = SimConfigService.INSTANCE.server().assembly.mergingGlueRange.getF();

        if (!(glue.is(SimTags.Items.MERGING_GLUE)) || distanceSquared > mergingGlueRange * mergingGlueRange) {
            return;
        }

        final BlockPos parentRelative = this.parentPos().relative(this.parentFacing);
        final BlockPos childRelative = this.childPos().relative(this.childFacing);

        final SubLevel parentSubLevel = Sable.HELPER.getContaining(level, parentRelative);
        final SubLevel childSubLevel = Sable.HELPER.getContaining(level, childRelative);
        if (parentSubLevel == null || childSubLevel == null) {
            return;

        }

        final MergingGlueBlockEntity controller = this.addMergingGlue(level, parentRelative, childRelative, this.parentFacing(), true, (float) distanceSquared);
        final MergingGlueBlockEntity partner = this.addMergingGlue(level, childRelative, parentRelative, this.childFacing(), false, (float) distanceSquared);

        if (controller == null || partner == null) {
            this.clearGlue(level, parentRelative, childRelative);
            return;
        }

        player.awardStat(Stats.ITEM_USED.get(glue.getItem()));
        controller.startControlling(partner);

        if (!controller.isController()) {
            // startControlling() bailed out because one of the sub-levels was no longer
            // available. Without a controlling glue the two placeholder blocks would sit
            // in the world for ten seconds doing nothing, so undo the placement.
            this.clearGlue(level, parentRelative, childRelative);
        }
    }

    private void clearGlue(final Level level, final BlockPos parentRelative, final BlockPos childRelative) {
        level.setBlockAndUpdate(parentRelative, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(childRelative, Blocks.AIR.defaultBlockState());
    }

    private MergingGlueBlockEntity addMergingGlue(final Level level, final BlockPos placedPos, final BlockPos childPos, final Direction facing, final boolean controller, final float distance) {
        // The client only predicts this when the spot is free, but the server has to check
        // too: setBlockAndUpdate() happily overwrites a real block, and the placeholder
        // glue would end up displacing whatever used to be here.
        if (!level.getBlockState(placedPos).canBeReplaced()) {
            return null;
        }

        final BlockState newState = SimBlocks.MERGING_GLUE.getDefaultState();

        if (level.setBlockAndUpdate(placedPos, newState.setValue(MergingGlueBlock.FACING, facing))) {
            final MergingGlueBlockEntity parentSpring = (MergingGlueBlockEntity) level.getBlockEntity(placedPos);

            if (parentSpring == null)
                return null;

            parentSpring.setPartnerPos(childPos);

            parentSpring.notifyUpdate();
            return parentSpring;
        }
        return null;
    }
}
