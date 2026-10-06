package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class BlockStateGen {
    private BlockStateGen() {
    }

    public record XYHolder(int xRot, int yRot) {
    }

    /**
     * 六向 + 轴向的方块（例如 borehead bearing、aeronautics 的 powered axis 方块）。
     * 旋转真值取自 1.21.1 生成的 {@code offroad:blockstates/borehead_bearing.json}：
     * 朝上绕 X 90°、朝下绕 X 270°、水平朝向只绕 Y（用 {@link Direction#toYRot()}，
     * south=0 / west=90 / north=180 / east=270），竖直朝向的 y 偏移由
     * {@code axis_along_first} 决定。
     *
     * @param models 状态 → 模型，第二参是该状态是否要用竖直模型
     */
    public static <T extends Block> void directionalAxisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                               BiFunction<BlockState, Boolean, ModelFile> models) {
        prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
            final boolean alongFirst = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
            final Direction direction = state.getValue(DirectionalAxisKineticBlock.FACING);
            final ConfiguredModel model = new ConfiguredModel(models.apply(state, usesVerticalModel(alongFirst, direction)));
            model.x = direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0;
            model.y = direction.getAxis().isVertical()
                    ? (alongFirst ? 0 : 90)
                    : (int) direction.toYRot();
            return new ConfiguredModel[]{model};
        });
    }

    /** 该状态是否需要竖直模型：水平朝向且朝向轴与 {@code axis_along_first} 一致。 */
    private static boolean usesVerticalModel(final boolean alongFirst, final Direction direction) {
        return direction.getAxis().isHorizontal()
                && (direction.getAxis() == Direction.Axis.X) == alongFirst;
    }

    /**
     * 绕 Y 轴的方块（例如 envelope encased shaft），旋转取自
     * 1.21.1 的 {@code axis=x -> x90,y90 / axis=y -> 不旋转 / axis=z -> x90,y180}。
     */
    public static <T extends Block> void axisBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                   Function<BlockState, ModelFile> models) {
        prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
            final ConfiguredModel model = new ConfiguredModel(models.apply(state));
            switch (state.getValue(BlockStateProperties.AXIS)) {
                case X -> {
                    model.x = 90;
                    model.y = 90;
                }
                case Z -> {
                    model.x = 90;
                    model.y = 180;
                }
                case Y -> { }
            }
            return new ConfiguredModel[]{model};
        });
    }

    /** 每个状态一个模型、不带旋转（例如 adjustable burner 的 variant 模型）。 */
    public static <T extends Block> void simpleBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                      Function<BlockState, ModelFile> models) {
        prov.getVariantBuilder(ctx.getEntry())
                .forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(models.apply(state))});
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalBlockProvider(boolean customItem) {
        return (ctx, prov) -> prov.directionalBlock(ctx.getEntry(), model(ctx, prov, customItem));
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalAxisBlockProvider(boolean customItem) {
        return (ctx, prov) -> {
            final Function<BlockState, ModelFile> model = model(ctx, prov, customItem);

            prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> new ConfiguredModel[]{
                    ConfiguredModel.builder()
                            .modelFile(model.apply(state))
                            .rotationY(state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0)
                            .build()});
        };
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalAxisBlockProvider() {
        return (ctx, prov) -> directionalAxisBlock(ctx, prov, (state, vertical) -> prov.models().getExistingFile(
                prov.modLoc("block/" + ctx.getName() + "/" + (vertical ? "vertical" : "horizontal"))));
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalBlockProvider(boolean customItem) {
        return (ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), model(ctx, prov, customItem));
    }

    /**
     * 默认 blockstate：每个状态都指向 {@code block/<name>}。
     * 仓库里没有任何调用点（历史上一直是空实现），这里给出一个完整可用的默认值，
     * 免得以后有人接上却静默产出空文件。
     */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> generate() {
        return (ctx, prov) -> prov.getVariantBuilder(ctx.getEntry())
                .forAllStates(state -> new ConfiguredModel[]{
                        new ConfiguredModel(prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName())))});
    }

    /**
     * 彩色方块的物品模型：直接继承 {@code block/<name>}。
     * 同样没有调用点，按 Create 的约定补上。
     */
    public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> coloredBlockItemModel() {
        return b -> b.model((ctx, prov) ->
                prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName()))).build();
    }

    /** {@code customItem} 时模型是 {@code block/<name>/block}，否则是 {@code block/<name>}。 */
    private static <T extends Block> Function<BlockState, ModelFile> model(final DataGenContext<Block, T> ctx,
                                                                           final RegistrateBlockstateProvider prov,
                                                                           final boolean customItem) {
        return customItem
                ? AssetLookup.partialBaseModel(ctx, prov)
                : state -> prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName()));
    }
}
