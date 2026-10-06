package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {

	// Forge AnimalTameEvent (horses, llamas...)
	@Inject(method = "tameWithName", at = @At("HEAD"))
	private void zeta$tame(Player player, CallbackInfoReturnable<Boolean> cir) {
		FabricZEntityEvents.fireAnimalTame((AbstractHorse) (Object) this, player);
	}
}
