package org.violetmoon.zetaimplfabric.item;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zeta.util.ToolActions;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Extensions for items that don't implement IZetaItemExtensions themselves. Fabric version of
 * zetaimplforge's IForgeItemItemExtensions: answers with what Forge's default IForgeItem would.
 */
public class FabricItemExtensions implements IZetaItemExtensions {

	public static final FabricItemExtensions INSTANCE = new FabricItemExtensions();

	@Override
	public boolean isRepairableZeta(ItemStack stack) {
		// Forge: canRepair (default true) && isDamageable
		return stack.isDamageableItem();
	}

	@Override
	public boolean canEquipZeta(ItemStack stack, EquipmentSlot slot, Entity ent) {
		return Mob.getEquipmentSlotForItem(stack) == slot;
	}

	@Override
	public boolean canShearZeta(ItemStack stack) {
		return ToolActions.canPerformAction(stack, ToolActions.SHEARS_CARVE);
	}

	@Override
	public int getBurnTimeZeta(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		return FabricZWorldEvents.getBurnTime(stack, recipeType);
	}
}
