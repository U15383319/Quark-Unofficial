package org.violetmoon.zeta.util;

import java.util.Set;

import org.violetmoon.zeta.item.ext.IZetaItemExtensions;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

/**
 * Fabric: stand-in for Forge's net.minecraftforge.common.ToolActions, including the default action sets
 * Forge patches onto the vanilla tool classes (IForgeItem#canPerformAction).
 */
public final class ToolActions {

	public static final ToolAction AXE_DIG = ToolAction.get("axe_dig");
	public static final ToolAction PICKAXE_DIG = ToolAction.get("pickaxe_dig");
	public static final ToolAction SHOVEL_DIG = ToolAction.get("shovel_dig");
	public static final ToolAction HOE_DIG = ToolAction.get("hoe_dig");
	public static final ToolAction SWORD_DIG = ToolAction.get("sword_dig");
	public static final ToolAction SHEARS_DIG = ToolAction.get("shears_dig");
	public static final ToolAction AXE_STRIP = ToolAction.get("axe_strip");
	public static final ToolAction AXE_SCRAPE = ToolAction.get("axe_scrape");
	public static final ToolAction AXE_WAX_OFF = ToolAction.get("axe_wax_off");
	public static final ToolAction SHOVEL_FLATTEN = ToolAction.get("shovel_flatten");
	public static final ToolAction SWORD_SWEEP = ToolAction.get("sword_sweep");
	public static final ToolAction SHEARS_HARVEST = ToolAction.get("shears_harvest");
	public static final ToolAction SHEARS_CARVE = ToolAction.get("shears_carve");
	public static final ToolAction SHEARS_DISARM = ToolAction.get("shears_disarm");
	public static final ToolAction HOE_TILL = ToolAction.get("till");
	public static final ToolAction SHIELD_BLOCK = ToolAction.get("shield_block");
	public static final ToolAction FISHING_ROD_CAST = ToolAction.get("fishing_rod_cast");

	public static final Set<ToolAction> DEFAULT_AXE_ACTIONS = Set.of(AXE_DIG, AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
	public static final Set<ToolAction> DEFAULT_HOE_ACTIONS = Set.of(HOE_DIG, HOE_TILL);
	public static final Set<ToolAction> DEFAULT_SHOVEL_ACTIONS = Set.of(SHOVEL_DIG, SHOVEL_FLATTEN);
	public static final Set<ToolAction> DEFAULT_PICKAXE_ACTIONS = Set.of(PICKAXE_DIG);
	public static final Set<ToolAction> DEFAULT_SWORD_ACTIONS = Set.of(SWORD_DIG, SWORD_SWEEP);
	public static final Set<ToolAction> DEFAULT_SHEARS_ACTIONS = Set.of(SHEARS_DIG, SHEARS_HARVEST, SHEARS_CARVE, SHEARS_DISARM);
	public static final Set<ToolAction> DEFAULT_SHIELD_ACTIONS = Set.of(SHIELD_BLOCK);
	public static final Set<ToolAction> DEFAULT_FISHING_ROD_ACTIONS = Set.of(FISHING_ROD_CAST);

	private ToolActions() { }

	/**
	 * Equivalent of Forge's ItemStack#canPerformAction.
	 */
	public static boolean canPerformAction(ItemStack stack, ToolAction action) {
		Item item = stack.getItem();
		if(action == SHEARS_CARVE && item instanceof IZetaItemExtensions ext)
			return ext.canShearZeta(stack);

		if(item instanceof AxeItem)
			return DEFAULT_AXE_ACTIONS.contains(action);
		if(item instanceof HoeItem)
			return DEFAULT_HOE_ACTIONS.contains(action);
		if(item instanceof ShovelItem)
			return DEFAULT_SHOVEL_ACTIONS.contains(action);
		if(item instanceof PickaxeItem)
			return DEFAULT_PICKAXE_ACTIONS.contains(action);
		if(item instanceof SwordItem)
			return DEFAULT_SWORD_ACTIONS.contains(action);
		if(item instanceof ShearsItem)
			return DEFAULT_SHEARS_ACTIONS.contains(action);
		if(item instanceof ShieldItem)
			return DEFAULT_SHIELD_ACTIONS.contains(action);
		if(item instanceof FishingRodItem)
			return DEFAULT_FISHING_ROD_ACTIONS.contains(action);
		return false;
	}
}
