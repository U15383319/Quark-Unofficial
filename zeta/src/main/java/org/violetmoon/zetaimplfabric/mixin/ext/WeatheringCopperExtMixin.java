package org.violetmoon.zetaimplfabric.mixin.ext;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zeta.block.ext.CustomWeatheringCopper;

import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric version of zetaimplforge's WeatheringCopperMixin: vanilla's static WeatheringCopper#getPrevious / #getFirst
 * (axe scraping, lightning) only know the blocks in vanilla's weathering maps, so CustomWeatheringCopper blocks (Quark's
 * copper vertical slabs) answer for themselves.
 */
@Mixin(WeatheringCopper.class)
public interface WeatheringCopperExtMixin {

	@Inject(method = "getPrevious(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
	private static void zeta$customPrevious(BlockState state, CallbackInfoReturnable<Optional<BlockState>> cir) {
		if(state.getBlock() instanceof CustomWeatheringCopper copper)
			cir.setReturnValue(copper.getPrevious(state));
	}

	@Inject(method = "getFirst(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"), cancellable = true)
	private static void zeta$customFirst(BlockState state, CallbackInfoReturnable<BlockState> cir) {
		if(state.getBlock() instanceof CustomWeatheringCopper copper)
			cir.setReturnValue(copper.getFirst(state));
	}
}
