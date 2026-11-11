package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

@Mixin(Zombie.class)
public abstract class ZombieMixin {

	/**
	 * Forge LivingConversionEvent.Pre: ForgeEventFactory.canLivingConvert is part of the villager-infection condition,
	 * so it fires before the 50% roll on Normal (ZombieVillagersOnNormalModule relies on that).
	 * <p>
	 * The third getDifficulty() call is the first thing inside that condition. (Not the "instanceof Villager" check:
	 * class constants in @At args aren't remapped to intermediary names.)
	 */
	@Inject(method = "killedEntity", cancellable = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDifficulty()Lnet/minecraft/world/Difficulty;", ordinal = 2))
	private void zeta$conversionPre(ServerLevel level, LivingEntity entity, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) boolean result) {
		if(!FabricZEntityEvents.fireConversionPre(entity, EntityType.ZOMBIE_VILLAGER))
			cir.setReturnValue(result);
	}
}
