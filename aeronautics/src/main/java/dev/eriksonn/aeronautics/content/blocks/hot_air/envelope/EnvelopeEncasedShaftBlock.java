package dev.eriksonn.aeronautics.content.blocks.hot_air.envelope;

import net.minecraft.util.Prediction;


import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EnvelopeEncasedShaftBlock extends EncasedShaftBlock implements Envelope, SpecialBlockItemRequirement {

    protected final DyeColor color;

    protected EnvelopeEncasedShaftBlock(final Properties properties, final DyeColor color) {
        super(properties.bounceRestitution(0.5F), AeroBlocks.DYED_ENVELOPE_BLOCKS.get(color));
        this.color = color;
    }

    public static EnvelopeEncasedShaftBlock withCanvas(final Properties properties, final DyeColor color) {
        return new EnvelopeEncasedShaftBlock(properties, color);
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack itemStack, final BlockState blockState, final Level level, final BlockPos blockPos, final Player player, final InteractionHand interactionHand, final BlockHitResult blockHitResult) {
        final DyeColor color = SimItemService.getDyeColor(itemStack);

        if (color != null) {
            if (!level.isClientSide())
                level.playSound(null, blockPos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f, 1.1f - level.getRandom().nextFloat() * .2f);

            EnvelopeBlock.applyDye(blockState, level, blockPos, color);
            return InteractionResult.SUCCESS;
        }


        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        super.onSneakWrenched(state, context);
        final Level world = context.getLevel();
        if (world instanceof ServerLevel) {
            final Player player = context.getPlayer();
            if (player != null && !player.hasInfiniteMaterials())
                player.getInventory().placeItemBackInInventory(AeroBlocks.WHITE_ENVELOPE_BLOCK.asStack(), Prediction.SERVER_ONLY);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return AeroBlockEntityTypes.ENVELOPE_ENCASED_SHAFT.get();
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return AeroBlockEntityTypes.ENVELOPE_ENCASED_SHAFT.create(pos, state);
    }

    @Override
    public void fallOn(final Level pLevel, final BlockState pState, final BlockPos pPos, final Entity pEntity, final double pFallDistance) {
        if (pEntity.isSuppressingBounce()) {
            super.fallOn(pLevel, pState, pPos, pEntity, pFallDistance);
        } else {
            pEntity.causeFallDamage(pFallDistance, 0.5F, pLevel.damageSources().fall());
        }
    }

    // 26.3 已经没有 Block#updateEntityMovementAfterFallOn：实体反弹改由
    // BlockBehaviour.Properties#bounceRestitution 驱动（在构造函数里设置），
    // 原来 bounceUp 用的 0.5/0.25 就是「非 LivingEntity 再乘一个系数」的比例。

    @Override
    public ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state, final boolean includeData) {
        return this.getCasing().asItem().getDefaultInstance();
    }

    @Override
    public Block getCasing() {
        return AeroBlocks.DYED_ENVELOPE_BLOCKS.get(this.color);
    }

    @Override
    public void handleEncasing(final BlockState state, final Level level, final BlockPos pos, final ItemStack heldItem, final Player player, final InteractionHand hand, final BlockHitResult ray) {
        super.handleEncasing(state, level, pos, heldItem, player, hand, ray);
        if (!player.hasInfiniteMaterials()) {
            player.getItemInHand(hand).shrink(1);
        }
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        ItemStack stack = AeroBlocks.WHITE_ENVELOPE_BLOCK.asStack();
        return super.getRequiredItems(state, be).union(new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, stack));
    }
}
