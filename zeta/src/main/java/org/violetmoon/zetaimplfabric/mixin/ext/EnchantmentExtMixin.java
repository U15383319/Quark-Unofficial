package org.violetmoon.zetaimplfabric.mixin.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.IZetaEnchantmentExtensions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Merges canApplyAtEnchantingTable into Enchantment itself (rather than leaving it as the interface default) so that,
 * as on Forge, other mixins can inject into it. Applied before the default priority for that reason.
 */
@Mixin(value = Enchantment.class, priority = 500)
public abstract class EnchantmentExtMixin implements IZetaEnchantmentExtensions {

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		Enchantment self = (Enchantment) (Object) this;
		if(stack.getItem() instanceof IZetaItemExtensions ext)
			return ext.canApplyAtEnchantingTableZeta(stack, self);
		return self.category.canEnchant(stack.getItem());
	}
}
