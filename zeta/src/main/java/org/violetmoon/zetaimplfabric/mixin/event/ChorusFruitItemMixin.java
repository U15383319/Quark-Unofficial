package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ChorusFruitItem;

@Mixin(ChorusFruitItem.class)
public abstract class ChorusFruitItemMixin {

	// Forge EntityTeleportEvent.ChorusFruit
	@WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;randomTeleport(DDDZ)Z"))
	private boolean zeta$teleport(LivingEntity entity, double x, double y, double z, boolean particles, Operation<Boolean> original) {
		FabricZEntityEvents.EntityTeleport event = FabricZEntityEvents.fireTeleport(entity, x, y, z);
		if(event.isCanceled())
			return false;
		return original.call(entity, event.getTargetX(), event.getTargetY(), event.getTargetZ(), particles);
	}
}
