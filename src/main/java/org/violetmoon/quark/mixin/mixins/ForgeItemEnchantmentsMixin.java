package org.violetmoon.quark.mixin.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.tweaks.module.GoldToolsHaveFortuneModule;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Fabric replacement for IForgeItemMixin: on Forge, Quark modified IForgeItem#getEnchantmentLevel and
 * #getAllEnchantments (through ForgeItemDelegate), which Forge's EnchantmentHelper consults.
 */
@Mixin(value = EnchantmentHelper.class, priority = 1100)
public class ForgeItemEnchantmentsMixin {

	@ModifyReturnValue(method = "getItemEnchantmentLevel", at = @At("RETURN"))
	private static int quark$getEnchantmentLevel(int previous, Enchantment enchantment, ItemStack stack) {
		return GoldToolsHaveFortuneModule.getActualEnchantmentLevel(enchantment, stack, previous);
	}

	// Forge iterates ItemStack#getAllEnchantments here
	@Inject(method = "runIterationOnItem", at = @At("HEAD"), cancellable = true)
	private static void quark$getAllEnchantments(EnchantmentHelper.EnchantmentVisitor visitor, ItemStack stack, CallbackInfo ci) {
		if(stack.isEmpty())
			return;

		Map<Enchantment, Integer> enchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
		int before = enchantments.size();
		GoldToolsHaveFortuneModule.addEnchantmentsIfMissing(stack, enchantments);
		if(enchantments.size() == before)
			return; // nothing added, let vanilla iterate

		enchantments.forEach(visitor::accept);
		ci.cancel();
	}
}
