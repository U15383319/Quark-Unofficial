package org.violetmoon.zetaimplfabric.mixin.event;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zeta.event.play.entity.living.ZLivingChangeTarget;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public abstract class MobMixin {

	// Forge LivingChangeTargetEvent (MOB_TARGET)
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void zeta$changeTarget(@Nullable LivingEntity target, CallbackInfo ci) {
		if(!FabricZEntityEvents.fireChangeTarget((Mob) (Object) this, target, ZLivingChangeTarget.LivingTargetType.MOB_TARGET))
			ci.cancel();
	}

	// Forge LivingConversionEvent.Post (vanilla funnels every mob conversion through convertTo)
	@Inject(method = "convertTo", at = @At("RETURN"))
	private <T extends Mob> void zeta$conversionPost(EntityType<T> type, boolean transferInventory, CallbackInfoReturnable<T> cir) {
		T outcome = cir.getReturnValue();
		if(outcome != null)
			FabricZEntityEvents.fireConversionPost((Mob) (Object) this, outcome);
	}
}
