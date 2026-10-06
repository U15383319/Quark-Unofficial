package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

	@Shadow @Final private DataSlot cost;
	@Shadow private int repairItemCountCost;
	@Shadow private String itemName;

	// the anvil damage roll happens in a static lambda, see zeta$breakChance
	@Unique
	private static float zeta$currentBreakChance = 0.12F;

	public AnvilMenuMixin(MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
		super(type, containerId, playerInventory, access);
	}

	// Forge AnvilUpdateEvent (ForgeHooks.onAnvilChange), right after the base repair cost is known
	@Inject(method = "createResult", cancellable = true,
		at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/AnvilMenu;repairItemCountCost:I", opcode = org.objectweb.asm.Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
	private void zeta$anvilUpdate(CallbackInfo ci, @Local(ordinal = 0) ItemStack left, @Local(ordinal = 2) ItemStack right, @Local(ordinal = 1) int baseCost) {
		FabricZWorldEvents.AnvilUpdate event = FabricZWorldEvents.fireAnvilUpdate(left, right, itemName, baseCost, player);
		if(event.isCanceled()) {
			ci.cancel();
			return;
		}

		if(!event.getOutput().isEmpty()) {
			resultSlots.setItem(0, event.getOutput());
			cost.set(event.getCost());
			repairItemCountCost = event.getMaterialCost();
			ci.cancel();
		}
	}

	// Forge AnvilRepairEvent
	@Inject(method = "onTake", at = @At("HEAD"))
	private void zeta$anvilRepair(Player player, ItemStack stack, CallbackInfo ci) {
		zeta$currentBreakChance = FabricZWorldEvents.fireAnvilRepair(player, inputSlots.getItem(0), inputSlots.getItem(1), stack);
	}

	@ModifyConstant(method = "lambda$onTake$2", constant = @Constant(floatValue = 0.12F))
	private static float zeta$breakChance(float vanilla) {
		return zeta$currentBreakChance;
	}
}
