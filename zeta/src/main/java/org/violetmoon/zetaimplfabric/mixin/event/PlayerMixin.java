package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.event.play.FabricZPlayerEvents;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Player.class)
public abstract class PlayerMixin {

	// Forge TickEvent.PlayerTickEvent
	@Inject(method = "tick", at = @At("HEAD"))
	private void zeta$tickStart(CallbackInfo ci) {
		FabricZPlayerEvents.fireTick((Player) (Object) this, true);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void zeta$tickEnd(CallbackInfo ci) {
		FabricZPlayerEvents.fireTick((Player) (Object) this, false);
	}

	// Forge PlayerEvent.BreakSpeed
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void zeta$breakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(FabricZPlayerEvents.fireBreakSpeed((Player) (Object) this, state, cir.getReturnValueF()));
	}
}
