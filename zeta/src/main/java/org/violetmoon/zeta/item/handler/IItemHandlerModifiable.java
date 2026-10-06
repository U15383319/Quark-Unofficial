package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric stand-in for Forge's net.minecraftforge.items.IItemHandlerModifiable.
 */
public interface IItemHandlerModifiable extends IItemHandler {
	void setStackInSlot(int slot, @NotNull ItemStack stack);
}
