package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

@Mixin(PersistentEntitySectionManager.class)
public abstract class PersistentEntitySectionManagerMixin<T extends EntityAccess> {

	// Forge EntityJoinLevelEvent on the server: both new entities and entities loaded with their chunk
	@Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
	private void zeta$joinLevel(T entity, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
		if(entity instanceof Entity e && !FabricZEntityEvents.fireJoinLevel(e))
			cir.setReturnValue(false);
	}
}
