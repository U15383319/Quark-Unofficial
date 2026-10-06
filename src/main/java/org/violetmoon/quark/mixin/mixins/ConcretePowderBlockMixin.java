package org.violetmoon.quark.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.tweaks.module.MagmaKeepsConcretePowderModule;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ConcretePowderBlock.class)
public class ConcretePowderBlockMixin {
	
	@Inject(method = "touchesLiquid", at = @At("HEAD"), cancellable = true)
	private static void touchesLiquid(BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cbi) {
		if(MagmaKeepsConcretePowderModule.preventSolidify(pLevel, pPos, null)) { // Fabric: vanilla touchesLiquid has no state parameter (unused)
			cbi.setReturnValue(false);
			cbi.cancel();
		}
	}
	
	@Inject(method = "shouldSolidify", at = @At("HEAD"), cancellable = true)
	private static void shouldSolidify(BlockGetter pLevel, BlockPos pPos, BlockState pState, CallbackInfoReturnable<Boolean> cbi) { // Fabric: Forge adds a FluidState parameter
		if(MagmaKeepsConcretePowderModule.preventSolidify(pLevel, pPos, pState)) {
			cbi.setReturnValue(false);
			cbi.cancel();
		}
	}

}
