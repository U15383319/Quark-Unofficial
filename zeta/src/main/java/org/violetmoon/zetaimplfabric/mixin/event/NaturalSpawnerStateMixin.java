package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerStateMixin {

	// Forge: IForgeEntity#getClassification decides which mob cap an entity counts towards
	@ModifyExpressionValue(method = "createState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;getCategory()Lnet/minecraft/world/entity/MobCategory;"))
	private static MobCategory zeta$classification(MobCategory original, @Local Entity entity) {
		return entity.getClassification(true);
	}
}
