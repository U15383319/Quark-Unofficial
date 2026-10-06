package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Forge AnvilMenu#createResult: no result when an enchanted book is applied to an item that refuses books
 * (IForgeItem#isBookEnchantable).
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuHookMixin extends ItemCombinerMenu {

	private AnvilMenuHookMixin(MenuType<?> type, int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(type, containerId, inventory, access);
	}

	@ModifyArg(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"), index = 1)
	private ItemStack zeta$isBookEnchantable(ItemStack result) {
		if(result.isEmpty())
			return result;
		ItemStack left = inputSlots.getItem(0);
		ItemStack right = inputSlots.getItem(1);
		boolean book = right.is(Items.ENCHANTED_BOOK) && !EnchantedBookItem.getEnchantments(right).isEmpty();
		IZetaItemExtensions ext = book ? ZetaHooks.item(left, "isBookEnchantableZeta") : null;
		return ext != null && !ext.isBookEnchantableZeta(left, right) ? ItemStack.EMPTY : result;
	}
}
