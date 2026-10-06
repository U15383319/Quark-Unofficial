package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;

/**
 * Forge attributes this mobGriefing check to the killer (the wither).
 */
@Mixin(LivingEntity.class)
public class LivingEntityGriefingMixin {

	@WrapOperation(method = "createWitherRose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
	private boolean zeta$griefer(GameRules rules, GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original, LivingEntity source) {
		Entity previous = FabricZEntityEvents.swapGriefer(source);
		try {
			return original.call(rules, key);
		} finally {
			FabricZEntityEvents.restoreGriefer(previous);
		}
	}
}
