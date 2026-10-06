package org.violetmoon.zetaimplfabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;

@Mixin(ItemColors.class)
public abstract class ItemColorsMixin {

	// Forge RegisterColorHandlersEvent.Item
	@Inject(method = "createDefault", at = @At("RETURN"))
	private static void zeta$registerColors(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir) {
		FabricZClientEvents.fireItemColors(cir.getReturnValue(), blockColors);
	}
}
