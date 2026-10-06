package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zeta.event.bus.ZResult;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {

	// Forge BonemealEvent (ForgeEventFactory.onApplyBonemeal)
	@Inject(method = "growCrop", at = @At("HEAD"), cancellable = true)
	private static void zeta$bonemeal(ItemStack stack, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		FabricZWorldEvents.Bonemeal event = FabricZWorldEvents.fireBonemeal(level, pos, level.getBlockState(pos), stack);
		if(event.isCanceled())
			cir.setReturnValue(false);
		else if(event.getResult() == ZResult.ALLOW) {
			if(!level.isClientSide)
				stack.shrink(1);
			cir.setReturnValue(true);
		}
	}
}
