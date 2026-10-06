package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric stand-in for Forge's net.minecraftforge.items.IItemHandler: a slotted item inventory with simulated
 * insert/extract. Bridged to the Fabric Transfer API by {@link ItemHandlers}.
 */
public interface IItemHandler {

	int getSlots();

	@NotNull
	ItemStack getStackInSlot(int slot);

	/**
	 * @return the remainder that could not be inserted
	 */
	@NotNull
	ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

	@NotNull
	ItemStack extractItem(int slot, int amount, boolean simulate);

	int getSlotLimit(int slot);

	default boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return true;
	}
}
