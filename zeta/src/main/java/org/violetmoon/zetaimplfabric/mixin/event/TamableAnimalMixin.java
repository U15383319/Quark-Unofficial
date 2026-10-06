package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {

	// Forge AnimalTameEvent (wolves, cats, parrots)
	@Inject(method = "tame", at = @At("HEAD"))
	private void zeta$tame(Player player, CallbackInfo ci) {
		FabricZEntityEvents.fireAnimalTame((TamableAnimal) (Object) this, player);
	}
}
