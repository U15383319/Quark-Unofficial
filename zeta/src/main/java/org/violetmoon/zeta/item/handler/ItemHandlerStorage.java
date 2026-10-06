package org.violetmoon.zeta.item.handler;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric Transfer {@link SlottedStorage} view of an IItemHandler, so hoppers and other mods can use Quark's inventories.
 * Changes are applied immediately and undone if the transaction is aborted.
 */
public class ItemHandlerStorage implements SlottedStorage<ItemVariant> {

	private final IItemHandler handler;
	private final List<Slot> slots = new ArrayList<>();

	public ItemHandlerStorage(IItemHandler handler) {
		this.handler = handler;
	}

	@Override
	public int getSlotCount() {
		return handler.getSlots();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		while(slots.size() <= slot)
			slots.add(new Slot(slots.size()));
		return slots.get(slot);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long inserted = 0;
		for(int i = 0; i < getSlotCount() && inserted < maxAmount; i++)
			inserted += getSlot(i).insert(resource, maxAmount - inserted, transaction);
		return inserted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long extracted = 0;
		for(int i = 0; i < getSlotCount() && extracted < maxAmount; i++)
			extracted += getSlot(i).extract(resource, maxAmount - extracted, transaction);
		return extracted;
	}

	@Override
	public java.util.Iterator<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant>> iterator() {
		List<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant>> views = new ArrayList<>();
		for(int i = 0; i < getSlotCount(); i++)
			views.add(getSlot(i));
		return views.iterator();
	}

	private final class Slot extends SnapshotParticipant<ItemStack> implements SingleSlotStorage<ItemVariant> {
		private final int index;

		Slot(int index) {
			this.index = index;
		}

		private ItemStack stack() {
			return index < handler.getSlots() ? handler.getStackInSlot(index) : ItemStack.EMPTY;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);
			int amount = (int) Math.min(maxAmount, resource.getItem().getMaxStackSize());
			ItemStack toInsert = resource.toStack(amount);
			ItemStack remainder = handler.insertItem(index, toInsert, true);
			int inserted = amount - remainder.getCount();
			if(inserted > 0) {
				updateSnapshots(transaction);
				handler.insertItem(index, resource.toStack(inserted), false);
			}
			return inserted;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);
			if(!resource.matches(stack()))
				return 0;
			ItemStack simulated = handler.extractItem(index, (int) Math.min(maxAmount, Integer.MAX_VALUE), true);
			if(simulated.isEmpty())
				return 0;
			updateSnapshots(transaction);
			return handler.extractItem(index, simulated.getCount(), false).getCount();
		}

		@Override
		public boolean isResourceBlank() {
			return stack().isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(stack());
		}

		@Override
		public long getAmount() {
			return stack().getCount();
		}

		@Override
		public long getCapacity() {
			return handler.getSlotLimit(index);
		}

		@Override
		protected ItemStack createSnapshot() {
			return stack().copy();
		}

		@Override
		protected void readSnapshot(ItemStack snapshot) {
			if(handler instanceof IItemHandlerModifiable modifiable)
				modifiable.setStackInSlot(index, snapshot);
		}
	}
}
