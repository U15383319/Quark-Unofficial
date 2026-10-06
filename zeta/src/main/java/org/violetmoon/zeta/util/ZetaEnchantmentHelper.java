package org.violetmoon.zeta.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Fabric: Forge renamed vanilla's tag lookup to getTagEnchantmentLevel and routed getItemEnchantmentLevel through the
 * item. Here getItemEnchantmentLevel is the hooked one (see EnchantmentHelper mixins), and this reads the raw tag.
 */
public final class ZetaEnchantmentHelper {

	private ZetaEnchantmentHelper() { }

	public static int getTagEnchantmentLevel(Enchantment enchantment, ItemStack stack) {
		if(stack.isEmpty())
			return 0;

		ResourceLocation id = EnchantmentHelper.getEnchantmentId(enchantment);
		ListTag list = stack.getEnchantmentTags();
		for(int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			ResourceLocation tagId = EnchantmentHelper.getEnchantmentId(tag);
			if(tagId != null && tagId.equals(id))
				return EnchantmentHelper.getEnchantmentLevel(tag);
		}
		return 0;
	}
}
