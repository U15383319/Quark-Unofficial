package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.event.play.FabricZPlayerEvents;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

	@Shadow private int pickupDelay;

	// Forge EntityItemPickupEvent: server side, once the pickup delay is over, before the item goes into the inventory
	@Inject(method = "playerTouch", at = @At("HEAD"))
	private void zeta$pickup(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if(!self.level().isClientSide && pickupDelay == 0)
			FabricZPlayerEvents.fireItemPickup(player, self);
	}
}
