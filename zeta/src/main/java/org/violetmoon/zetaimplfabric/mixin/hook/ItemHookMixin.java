package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Forge: the durability bar uses the stack-sensitive getMaxDamage(stack).
 */
@Mixin(Item.class)
public class ItemHookMixin {

	@ModifyExpressionValue(method = { "getBarWidth", "getBarColor" }, at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Item;maxDamage:I"))
	private int zeta$maxDamage(int maxDamage, ItemStack stack) {
		IZetaItemExtensions ext = ZetaHooks.item(stack, "getMaxDamageZeta");
		return ext != null ? ext.getMaxDamageZeta(stack) : maxDamage;
	}
}
