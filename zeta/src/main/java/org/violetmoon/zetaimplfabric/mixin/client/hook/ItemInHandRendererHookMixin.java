package org.violetmoon.zetaimplfabric.mixin.client.hook;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

/**
 * Forge ForgeHooksClient#shouldCauseReequipAnimation: the item can keep the hand from dipping when its stack changes
 * (IForgeItem#shouldCauseReequipAnimation). A "match" makes vanilla adopt the new stack without the animation.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererHookMixin {

	@Shadow @Final private Minecraft minecraft;

	@Unique private int zeta$lastSlot = -1;

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 0))
	private boolean zeta$mainHandReequip(ItemStack from, ItemStack to, Operation<Boolean> original) {
		int slot = minecraft.player.getInventory().selected;
		boolean slotChanged = zeta$lastSlot != -1 && slot != zeta$lastSlot;
		zeta$lastSlot = slot;
		return zeta$matches(from, to, slotChanged, original);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 1))
	private boolean zeta$offHandReequip(ItemStack from, ItemStack to, Operation<Boolean> original) {
		return zeta$matches(from, to, false, original);
	}

	@Unique
	private static boolean zeta$matches(ItemStack from, ItemStack to, boolean slotChanged, Operation<Boolean> original) {
		IZetaItemExtensions ext = to.isEmpty() ? null : ZetaHooks.item(from, "shouldCauseReequipAnimationZeta");
		return ext != null ? !ext.shouldCauseReequipAnimationZeta(from, to, slotChanged) : original.call(from, to);
	}
}
