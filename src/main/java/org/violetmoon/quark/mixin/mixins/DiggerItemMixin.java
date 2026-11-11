package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.violetmoon.quark.content.tweaks.module.GoldToolsHaveFortuneModule;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {

	// Fabric: Forge's isCorrectToolForDrops(ItemStack, BlockState) doesn't exist, the vanilla method takes only the state
	@ModifyExpressionValue(
		method = "isCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DiggerItem;getTier()Lnet/minecraft/world/item/Tier;")
	)
	private Tier getTier(Tier prev) {
		return GoldToolsHaveFortuneModule.getEffectiveTier(prev);
	}

}
