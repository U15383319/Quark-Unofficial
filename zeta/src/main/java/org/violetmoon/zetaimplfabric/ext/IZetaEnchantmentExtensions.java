package org.violetmoon.zetaimplfabric.ext;

import org.violetmoon.zeta.item.ext.IZetaItemExtensions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Forge: Enchantment#canApplyAtEnchantingTable and #isAllowedOnBooks, injected with Loom interface injection.
 */
public interface IZetaEnchantmentExtensions {
	default boolean canApplyAtEnchantingTable(ItemStack stack) {
		Enchantment self = (Enchantment) this;
		if(stack.getItem() instanceof IZetaItemExtensions ext)
			return ext.canApplyAtEnchantingTableZeta(stack, self);
		return self.category.canEnchant(stack.getItem());
	}

	default boolean isAllowedOnBooks() {
		return true;
	}
}
