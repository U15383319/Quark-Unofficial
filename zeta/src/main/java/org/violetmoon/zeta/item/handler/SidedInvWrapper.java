package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric stand-in for Forge's net.minecraftforge.items.wrapper.SidedInvWrapper: only exposes the slots of one face.
 */
public class SidedInvWrapper implements IItemHandlerModifiable {

	private final WorldlyContainer inv;
	private final @Nullable Direction side;
	private final InvWrapper raw;

	public SidedInvWrapper(WorldlyContainer inv, @Nullable Direction side) {
		this.inv = inv;
		this.side = side;
		this.raw = new InvWrapper(inv);
	}

	private int[] slots() {
		return inv.getSlotsForFace(side == null ? Direction.UP : side);
	}

	private int slot(int index) {
		return slots()[index];
	}

	@Override
	public int getSlots() {
		return slots().length;
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return inv.getItem(slot(slot));
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		int real = slot(slot);
		if(!inv.canPlaceItemThroughFace(real, stack, side))
			return stack;
		return raw.insertItem(real, stack, simulate);
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		int real = slot(slot);
		ItemStack inSlot = inv.getItem(real);
		if(inSlot.isEmpty() || !inv.canTakeItemThroughFace(real, inSlot, side))
			return ItemStack.EMPTY;
		return raw.extractItem(real, amount, simulate);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		inv.setItem(slot(slot), stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return inv.canPlaceItem(slot(slot), stack);
	}
}
