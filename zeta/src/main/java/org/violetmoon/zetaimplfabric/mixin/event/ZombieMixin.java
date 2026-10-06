package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

@Mixin(Zombie.class)
public abstract class ZombieMixin {

	// Forge LivingConversionEvent.Pre: ForgeEventFactory.canLivingConvert is part of the villager-infection condition
	@ModifyExpressionValue(method = "killedEntity", at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/entity/npc/Villager", ordinal = 0))
	private boolean zeta$conversionPre(boolean isVillager, ServerLevel level, LivingEntity entity) {
		return isVillager && FabricZEntityEvents.fireConversionPre(entity, EntityType.ZOMBIE_VILLAGER);
	}
}
