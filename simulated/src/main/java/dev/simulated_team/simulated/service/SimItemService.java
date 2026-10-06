package dev.simulated_team.simulated.service;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

public interface SimItemService {

    SimItemService INSTANCE = ServiceUtil.load(SimItemService.class);

    static DyeColor getDyeColor(final ItemStack itemStack) {
        return itemStack.getItem() instanceof DyeItem ? itemStack.get(DataComponents.DYE) : null;
    }

    int getBurnTime(final ItemStack stack);

    int getSuperheatedBurnTime(final ItemStack stack);
}
