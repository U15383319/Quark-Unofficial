package org.violetmoon.zeta.event.play.loading;

import org.violetmoon.zeta.event.bus.Cancellable;
import org.violetmoon.zeta.event.bus.IZetaPlayEvent;
import org.violetmoon.zeta.mixin.mixins.AccessorLootTable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public interface ZLootTableLoad extends IZetaPlayEvent, Cancellable {
	ResourceLocation getName();
	LootTable getTable();
	void setTable(LootTable table);

	default void add(LootPoolEntryContainer entry) {
		LootTable table = getTable();

		LootPool[] pools = ((AccessorLootTable) table).zeta$getPools();
		if (pools != null && pools.length > 0) {
			LootPool firstPool = pools[0];
			LootPoolEntryContainer[] entries = firstPool.entries;

			LootPoolEntryContainer[] newEntries = new LootPoolEntryContainer[entries.length + 1];
			System.arraycopy(entries, 0, newEntries, 0, entries.length);

			newEntries[entries.length] = entry;
			firstPool.entries = newEntries;
		}
	}
}
