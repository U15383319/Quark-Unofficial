package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Fabric stand-in for Forge's net.minecraftforge.items.ItemHandlerHelper.
 */
public final class ItemHandlerHelper {

	private ItemHandlerHelper() { }

	@NotNull
	public static ItemStack insertItem(@Nullable IItemHandler dest, @NotNull ItemStack stack, boolean simulate) {
		if(dest == null || stack.isEmpty())
			return stack;

		for(int i = 0; i < dest.getSlots(); i++) {
			stack = dest.insertItem(i, stack, simulate);
			if(stack.isEmpty())
				return ItemStack.EMPTY;
		}
		return stack;
	}

	public static boolean canItemStacksStack(@NotNull ItemStack a, @NotNull ItemStack b) {
		if(a.isEmpty() || !ItemStack.isSameItem(a, b) || a.hasTag() != b.hasTag())
			return false;
		return !a.hasTag() || a.getTag().equals(b.getTag());
	}

	/**
	 * Like canItemStacksStack, ignoring stack size limits (used for stacking into partially filled slots).
	 */
	public static boolean canItemStacksStackRelaxed(@NotNull ItemStack a, @NotNull ItemStack b) {
		if(a.isEmpty() || b.isEmpty() || a.getItem() != b.getItem() || !a.isStackable())
			return false;
		return ItemStack.isSameItemSameTags(a, b);
	}

	@NotNull
	public static ItemStack copyStackWithSize(@NotNull ItemStack stack, int size) {
		if(size == 0)
			return ItemStack.EMPTY;
		ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}

	/**
	 * Fills existing stacks first, then empty slots.
	 */
	@NotNull
	public static ItemStack insertItemStacked(@Nullable IItemHandler inventory, @NotNull ItemStack stack, boolean simulate) {
		if(inventory == null || stack.isEmpty())
			return stack;

		if(!stack.isStackable())
			return insertItem(inventory, stack, simulate);

		int sizeInventory = inventory.getSlots();
		for(int i = 0; i < sizeInventory; i++) {
			ItemStack slot = inventory.getStackInSlot(i);
			if(canItemStacksStackRelaxed(slot, stack)) {
				stack = inventory.insertItem(i, stack, simulate);
				if(stack.isEmpty())
					break;
			}
		}

		if(!stack.isEmpty()) {
			for(int i = 0; i < sizeInventory; i++) {
				if(inventory.getStackInSlot(i).isEmpty()) {
					stack = inventory.insertItem(i, stack, simulate);
					if(stack.isEmpty())
						break;
				}
			}
		}
		return stack;
	}

	/**
	 * Gives the item to the player, dropping whatever doesn't fit.
	 */
	public static void giveItemToPlayer(Player player, @NotNull ItemStack stack) {
		giveItemToPlayer(player, stack, -1);
	}

	public static void giveItemToPlayer(Player player, @NotNull ItemStack stack, int preferredSlot) {
		if(stack.isEmpty())
			return;

		IItemHandler inventory = new PlayerMainInvWrapper(player.getInventory());
		Level level = player.level();

		ItemStack remainder = stack;
		if(preferredSlot >= 0 && preferredSlot < inventory.getSlots())
			remainder = inventory.insertItem(preferredSlot, stack, false);
		if(!remainder.isEmpty())
			remainder = insertItemStacked(inventory, remainder, false);

		if(remainder.isEmpty() || remainder.getCount() != stack.getCount())
			level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
				0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

		if(!remainder.isEmpty() && !level.isClientSide) {
			ItemEntity entity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
			entity.setPickUpDelay(40);
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
			level.addFreshEntity(entity);
		}
	}
}
