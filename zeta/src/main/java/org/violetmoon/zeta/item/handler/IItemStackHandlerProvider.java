package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

/**
 * Replaces Forge's IForgeItem#initCapabilities providing an ITEM_HANDLER for item stacks. Implemented by the Item.
 */
public interface IItemStackHandlerProvider {
	@Nullable
	IItemHandler getItemHandler(ItemStack stack);
}
