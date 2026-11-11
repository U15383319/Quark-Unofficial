package org.violetmoon.quark.mixin.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.mixin.delegates.ForgeItemDelegate;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Effective copy of IForgeItemMixin (gold tools' baked-in enchantments), on Zeta's item extensions since Zeta should
 * not depend on Quark code.
 * <p>
 * Fabric: upstream used Zeta's @DelegateInterfaceMixin here, which matches the target by its descriptor string. That
 * string is never remapped, so in a released (intermediary-named) jar the modifier silently did nothing; this is a
 * plain interface mixin, which the refmap covers.
 */
@Mixin(IZetaItemExtensions.class)
public interface IZetaItemExtensionsMixin {

	@ModifyReturnValue(method = "getEnchantmentLevelZeta", at = @At("RETURN"))
	private int quark$getEnchantmentLevel(int previous, ItemStack stack, Enchantment enchantment) {
		return ForgeItemDelegate.getEnchantmentLevel(previous, stack, enchantment);
	}

	@ModifyReturnValue(method = "getAllEnchantmentsZeta", at = @At("RETURN"))
	private Map<Enchantment, Integer> quark$getAllEnchantments(Map<Enchantment, Integer> previous, ItemStack stack) {
		return ForgeItemDelegate.getAllEnchantments(previous, stack);
	}
}
