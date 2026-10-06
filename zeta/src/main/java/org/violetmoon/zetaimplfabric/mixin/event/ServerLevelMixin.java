package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * While an entity ticks, mobGriefing reads are attributed to it (FabricZEntityEvents#fireMobGriefing).
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin {

	@WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void zeta$tickGriefer(Entity entity, Operation<Void> original) {
		Entity previous = FabricZEntityEvents.swapGriefer(entity);
		try {
			original.call(entity);
		} finally {
			FabricZEntityEvents.restoreGriefer(previous);
		}
	}

	@WrapOperation(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
	private void zeta$rideTickGriefer(Entity passenger, Operation<Void> original) {
		Entity previous = FabricZEntityEvents.swapGriefer(passenger);
		try {
			original.call(passenger);
		} finally {
			FabricZEntityEvents.restoreGriefer(previous);
		}
	}
}
