package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.monster.EnderMan;

@Mixin(EnderMan.class)
public abstract class EnderManMixin {

	// Forge EntityTeleportEvent.EnderEntity
	@WrapOperation(method = "teleport(DDD)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;randomTeleport(DDDZ)Z"))
	private boolean zeta$teleport(EnderMan entity, double x, double y, double z, boolean particles, Operation<Boolean> original) {
		FabricZEntityEvents.EntityTeleport event = FabricZEntityEvents.fireTeleport(entity, x, y, z);
		if(event.isCanceled())
			return false;
		return original.call(entity, event.getTargetX(), event.getTargetY(), event.getTargetZ(), particles);
	}
}
