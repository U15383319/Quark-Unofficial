package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric stand-in for Forge's net.minecraftforge.items.wrapper.PlayerMainInvWrapper: the 36 main slots only.
 */
public class PlayerMainInvWrapper implements IItemHandlerModifiable {

	private final Inventory inv;
	private final InvWrapper raw;

	public PlayerMainInvWrapper(Inventory inv) {
		this.inv = inv;
		this.raw = new InvWrapper(inv);
	}

	@Override
	public int getSlots() {
		return inv.items.size();
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return inv.getItem(slot);
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		ItemStack rest = raw.insertItem(slot, stack, simulate);
		if(rest.getCount() != stack.getCount() && !simulate) {
			ItemStack inSlot = getStackInSlot(slot);
			if(!inSlot.isEmpty())
				inSlot.setPopTime(5);
		}
		return rest;
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return raw.extractItem(slot, amount, simulate);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		inv.setItem(slot, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}
}
