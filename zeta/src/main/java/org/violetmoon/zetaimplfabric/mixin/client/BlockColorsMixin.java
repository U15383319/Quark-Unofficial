package org.violetmoon.zetaimplfabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import net.minecraft.client.color.block.BlockColors;

@Mixin(BlockColors.class)
public abstract class BlockColorsMixin {

	// Forge RegisterColorHandlersEvent.Block
	@Inject(method = "createDefault", at = @At("RETURN"))
	private static void zeta$registerColors(CallbackInfoReturnable<BlockColors> cir) {
		FabricZClientEvents.fireBlockColors(cir.getReturnValue());
	}
}
