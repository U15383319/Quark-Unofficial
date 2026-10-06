package org.violetmoon.zetaimplfabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

	// Forge EntityJoinLevelEvent on the client
	@Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
	private void zeta$joinLevel(int entityId, Entity entity, CallbackInfo ci) {
		if(!FabricZEntityEvents.fireJoinLevel(entity))
			ci.cancel();
	}
}
