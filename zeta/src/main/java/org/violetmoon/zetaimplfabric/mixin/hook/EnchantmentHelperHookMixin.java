package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Forge: the enchanting table uses the stack-sensitive ItemStack#getEnchantmentValue (IForgeItem#getEnchantmentValue).
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperHookMixin {

	@WrapOperation(method = "getEnchantmentCost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
	private static int zeta$costEnchantmentValue(Item item, Operation<Integer> original, RandomSource random, int enchantNum, int power, ItemStack stack) {
		return enchantmentValue(item, original, stack);
	}

	@WrapOperation(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
	private static int zeta$selectEnchantmentValue(Item item, Operation<Integer> original, RandomSource random, ItemStack stack, int level, boolean allowTreasure) {
		return enchantmentValue(item, original, stack);
	}

	private static int enchantmentValue(Item item, Operation<Integer> original, ItemStack stack) {
		IZetaItemExtensions ext = ZetaHooks.item(stack, "getEnchantmentValueZeta");
		return ext != null ? ext.getEnchantmentValueZeta(stack) : original.call(item);
	}
}
