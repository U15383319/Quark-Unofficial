package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin {

	// Forge EntityTeleportEvent.EnderPearl
	@WrapOperation(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportTo(DDD)V"))
	private void zeta$teleport(Entity entity, double x, double y, double z, Operation<Void> original) {
		FabricZEntityEvents.EntityTeleport event = FabricZEntityEvents.fireTeleport(entity, x, y, z);
		if(!event.isCanceled())
			original.call(entity, event.getTargetX(), event.getTargetY(), event.getTargetZ());
	}
}
