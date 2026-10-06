package org.violetmoon.zetaimplfabric.mixin.event;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;

@Mixin(Animal.class)
public abstract class AnimalMixin {

	@Shadow
	public abstract @Nullable ServerPlayer getLoveCause();

	// Forge BabyEntitySpawnEvent; a null child cancels the breeding, like a canceled event on Forge
	@ModifyExpressionValue(method = "spawnChildFromBreeding", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;"))
	private @Nullable AgeableMob zeta$babySpawn(@Nullable AgeableMob child, ServerLevel level, Animal mate) {
		ServerPlayer cause = getLoveCause();
		if(cause == null)
			cause = mate.getLoveCause();
		return FabricZEntityEvents.fireBabySpawn((Animal) (Object) this, mate, cause, child);
	}
}
