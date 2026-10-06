package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

	// Forge LivingDeathEvent; ServerPlayer#die does not call super
	@Inject(method = "die", at = @At("HEAD"))
	private void zeta$playerDeath(DamageSource source, CallbackInfo ci) {
		FabricZEntityEvents.fireLivingDeath((ServerPlayer) (Object) this, source);
	}

	// Forge: player inventory drops are captured for LivingDropsEvent too
	@WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean zeta$captureDrop(Level level, Entity entity, Operation<Boolean> original, ItemStack stack, boolean dropAround, boolean includeThrowerName) {
		Collection<ItemEntity> captured = ((ServerPlayer) (Object) this).captureDrops();
		if(captured != null && entity instanceof ItemEntity item) {
			captured.add(item);
			return true;
		}
		return original.call(level, entity);
	}
}
