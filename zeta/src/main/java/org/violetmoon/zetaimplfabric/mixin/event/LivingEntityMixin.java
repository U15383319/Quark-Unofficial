package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	@Shadow protected int lastHurtByPlayerTime;

	public LivingEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	// Forge LivingEvent.LivingTickEvent
	@Inject(method = "tick", at = @At("HEAD"))
	private void zeta$livingTick(CallbackInfo ci) {
		FabricZEntityEvents.fireLivingTick((LivingEntity) (Object) this);
	}

	// Forge LivingDeathEvent (ServerPlayer#die is handled in ServerPlayerMixin, it doesn't call super)
	@Inject(method = "die", at = @At("HEAD"))
	private void zeta$livingDeath(DamageSource source, CallbackInfo ci) {
		FabricZEntityEvents.fireLivingDeath((LivingEntity) (Object) this, source);
	}

	// Forge LivingDropsEvent: capture everything dropped while dropping death loot, then let listeners edit/cancel it
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void zeta$startCapturingDrops(DamageSource source, CallbackInfo ci) {
		captureDrops(new ArrayList<>());
	}

	@Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
	private void zeta$fireDrops(DamageSource source, CallbackInfo ci) {
		Collection<ItemEntity> drops = captureDrops(null);
		if(drops == null)
			return;

		int looting = source.getEntity() instanceof Player p ? EnchantmentHelper.getMobLooting(p) : 0;
		if(FabricZEntityEvents.fireLivingDrops((LivingEntity) (Object) this, source, drops, looting, lastHurtByPlayerTime > 0))
			drops.forEach(e -> level().addFreshEntity(e));
	}

	// Forge LivingFallEvent
	@ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float zeta$livingFall(float fallDistance) {
		return FabricZEntityEvents.fireLivingFall((LivingEntity) (Object) this, fallDistance);
	}
}
