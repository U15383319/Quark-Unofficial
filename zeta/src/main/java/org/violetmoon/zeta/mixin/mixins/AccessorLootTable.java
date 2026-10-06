package org.violetmoon.zeta.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LootTable.class)
public interface AccessorLootTable {

	// Fabric: vanilla keeps an array (Forge patches it into a List)
	@Accessor("pools")
	LootPool[] zeta$getPools();

}
