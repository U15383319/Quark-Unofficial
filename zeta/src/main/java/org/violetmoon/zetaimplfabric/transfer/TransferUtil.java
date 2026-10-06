package org.violetmoon.zetaimplfabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

/**
 * Helpers that give Forge IItemHandler-style "stack in, remainder out" semantics on top of the Fabric Transfer API.
 */
public final class TransferUtil {

	private TransferUtil() { }

	/**
	 * Opens a transaction that is safe to use whether or not another transaction is already open on this thread.
	 */
	public static Transaction openTransaction() {
		return Transaction.isOpen() ? Transaction.openNested(Transaction.getCurrentUnsafe()) : Transaction.openOuter();
	}

	/**
	 * Equivalent of Forge's ItemHandlerHelper.insertItem(handler, stack, simulate).
	 *
	 * @return the part of the stack that could not be inserted
	 */
	public static ItemStack insert(Storage<ItemVariant> storage, ItemStack stack, boolean simulate) {
		if(stack.isEmpty())
			return ItemStack.EMPTY;

		long inserted;
		try(Transaction tx = openTransaction()) {
			inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
			if(!simulate)
				tx.commit();
		}

		if(inserted >= stack.getCount())
			return ItemStack.EMPTY;

		ItemStack remainder = stack.copy();
		remainder.setCount(stack.getCount() - (int) inserted);
		return remainder;
	}

	/**
	 * Extracts up to {@code amount} items matching the given variant.
	 *
	 * @return the extracted stack (possibly empty)
	 */
	public static ItemStack extract(Storage<ItemVariant> storage, ItemVariant variant, int amount, boolean simulate) {
		if(variant.isBlank() || amount <= 0)
			return ItemStack.EMPTY;

		long extracted;
		try(Transaction tx = openTransaction()) {
			extracted = storage.extract(variant, amount, tx);
			if(!simulate)
				tx.commit();
		}

		return extracted <= 0 ? ItemStack.EMPTY : variant.toStack((int) extracted);
	}

	/**
	 * @return whether the storage has any non-empty view
	 */
	public static boolean hasAnyItem(Storage<ItemVariant> storage) {
		for(StorageView<ItemVariant> view : storage)
			if(!view.isResourceBlank() && view.getAmount() > 0)
				return true;
		return false;
	}
}
