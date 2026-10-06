package org.violetmoon.zetaimplfabric.ext;

import org.violetmoon.zeta.util.ToolAction;
import org.violetmoon.zeta.util.ToolActions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * The Forge IForgeItemStack methods Quark calls, injected onto ItemStack with Loom interface injection.
 */
public interface IZetaItemStackExtensions {

	/**
	 * Forge: ItemStack#serializeNBT, which is just save() into a fresh tag.
	 */
	default CompoundTag serializeNBT() {
		return ((ItemStack) this).save(new CompoundTag());
	}

	default boolean canPerformAction(ToolAction action) {
		return ToolActions.canPerformAction((ItemStack) this, action);
	}
}
