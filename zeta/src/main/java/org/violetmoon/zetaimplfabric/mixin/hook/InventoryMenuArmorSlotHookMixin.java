package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Forge: the player inventory's armor slots accept what IForgeItem#canEquip allows.
 */
@Mixin(targets = "net.minecraft.world.inventory.InventoryMenu$1")
public abstract class InventoryMenuArmorSlotHookMixin extends Slot {

	// InventoryMenu.SLOT_IDS: the armor slot for inventory index 39 - i
	@Unique
	private static final EquipmentSlot[] ZETA_ARMOR_SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

	private InventoryMenuArmorSlotHookMixin(Container container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	@ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
	private boolean zeta$canEquip(boolean original, ItemStack stack) {
		IZetaItemExtensions ext = ZetaHooks.item(stack, "canEquipZeta");
		if(ext == null || !(container instanceof Inventory inventory))
			return original;
		int index = 39 - getContainerSlot();
		if(index < 0 || index >= ZETA_ARMOR_SLOTS.length)
			return original;
		return ext.canEquipZeta(stack, ZETA_ARMOR_SLOTS[index], inventory.player);
	}
}
