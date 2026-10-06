package org.violetmoon.zeta.item.handler;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.violetmoon.zetaimplfabric.transfer.TransferUtil;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

/**
 * IItemHandler view of a Fabric Transfer {@link Storage}, for inventories from other mods. Slots are the storage's
 * views (or its slots, for a SlottedStorage).
 */
public class StorageItemHandler implements IItemHandler {

	private final Storage<ItemVariant> storage;

	public StorageItemHandler(Storage<ItemVariant> storage) {
		this.storage = storage;
	}

	public Storage<ItemVariant> getStorage() {
		return storage;
	}

	private List<StorageView<ItemVariant>> views() {
		if(storage instanceof SlottedStorage<ItemVariant> slotted)
			return new ArrayList<>(slotted.getSlots());

		List<StorageView<ItemVariant>> list = new ArrayList<>();
		for(StorageView<ItemVariant> view : storage)
			list.add(view);
		return list;
	}

	@Override
	public int getSlots() {
		return views().size();
	}

	@Override
	@NotNull
	public ItemStack getStackInSlot(int slot) {
		List<StorageView<ItemVariant>> views = views();
		if(slot < 0 || slot >= views.size())
			return ItemStack.EMPTY;
		StorageView<ItemVariant> view = views.get(slot);
		return view.isResourceBlank() ? ItemStack.EMPTY : view.getResource().toStack((int) Math.min(view.getAmount(), Integer.MAX_VALUE));
	}

	@Override
	@NotNull
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if(stack.isEmpty())
			return ItemStack.EMPTY;

		long inserted;
		try(Transaction tx = TransferUtil.openTransaction()) {
			if(storage instanceof SlottedStorage<ItemVariant> slotted && slot >= 0 && slot < slotted.getSlotCount())
				inserted = slotted.getSlot(slot).insert(ItemVariant.of(stack), stack.getCount(), tx);
			else
				inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
			if(!simulate)
				tx.commit();
		}
		return inserted >= stack.getCount() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - (int) inserted);
	}

	@Override
	@NotNull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		List<StorageView<ItemVariant>> views = views();
		if(amount <= 0 || slot < 0 || slot >= views.size())
			return ItemStack.EMPTY;

		StorageView<ItemVariant> view = views.get(slot);
		if(view.isResourceBlank())
			return ItemStack.EMPTY;

		ItemVariant variant = view.getResource();
		long extracted;
		try(Transaction tx = TransferUtil.openTransaction()) {
			extracted = view.extract(variant, Math.min(amount, variant.getItem().getMaxStackSize()), tx);
			if(!simulate)
				tx.commit();
		}
		return extracted <= 0 ? ItemStack.EMPTY : variant.toStack((int) extracted);
	}

	@Override
	public int getSlotLimit(int slot) {
		List<StorageView<ItemVariant>> views = views();
		if(slot < 0 || slot >= views.size())
			return 0;
		return (int) Math.min(views.get(slot).getCapacity(), Integer.MAX_VALUE);
	}
}
