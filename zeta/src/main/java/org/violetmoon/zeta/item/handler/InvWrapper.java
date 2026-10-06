package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric stand-in for Forge's net.minecraftforge.items.wrapper.InvWrapper.
 */
public class InvWrapper implements IItemHandlerModifiable {

	private final Container inv;

	public InvWrapper(Container inv) {
		this.inv = inv;
	}

	public Container getInv() {
		return inv;
	}

	@Override
	public int getSlots() {
		return inv.getContainerSize();
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		return inv.getItem(slot);
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if(stack.isEmpty())
			return ItemStack.EMPTY;

		ItemStack stackInSlot = inv.getItem(slot);
		int m;
		if(!stackInSlot.isEmpty()) {
			if(stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot)))
				return stack;
			if(!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
				return stack;
			if(!inv.canPlaceItem(slot, stack))
				return stack;

			m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();
			if(stack.getCount() <= m) {
				if(!simulate) {
					ItemStack copy = stack.copy();
					copy.grow(stackInSlot.getCount());
					inv.setItem(slot, copy);
					inv.setChanged();
				}
				return ItemStack.EMPTY;
			}

			stack = stack.copy();
			if(!simulate) {
				ItemStack copy = stack.split(m);
				copy.grow(stackInSlot.getCount());
				inv.setItem(slot, copy);
				inv.setChanged();
			} else
				stack.shrink(m);
			return stack;
		}

		if(!inv.canPlaceItem(slot, stack))
			return stack;

		m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
		if(m < stack.getCount()) {
			stack = stack.copy();
			if(!simulate) {
				inv.setItem(slot, stack.split(m));
				inv.setChanged();
			} else
				stack.shrink(m);
			return stack;
		}

		if(!simulate) {
			inv.setItem(slot, stack);
			inv.setChanged();
		}
		return ItemStack.EMPTY;
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if(amount == 0)
			return ItemStack.EMPTY;

		ItemStack stackInSlot = inv.getItem(slot);
		if(stackInSlot.isEmpty())
			return ItemStack.EMPTY;

		if(simulate) {
			if(stackInSlot.getCount() < amount)
				return stackInSlot.copy();
			ItemStack copy = stackInSlot.copy();
			copy.setCount(amount);
			return copy;
		}

		int m = Math.min(stackInSlot.getCount(), amount);
		ItemStack decrStackSize = inv.removeItem(slot, m);
		inv.setChanged();
		return decrStackSize;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		inv.setItem(slot, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return inv.canPlaceItem(slot, stack);
	}
}
