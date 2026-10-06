package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Forge ItemEntity#tick: the item can take over the tick (IForgeItem#onEntityItemUpdate returning true).
 */
@Mixin(ItemEntity.class)
public class ItemEntityHookMixin {

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void zeta$onEntityItemUpdate(CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		ItemStack stack = self.getItem();
		IZetaItemExtensions ext = ZetaHooks.item(stack, "onEntityItemUpdateZeta");
		if(ext != null && ext.onEntityItemUpdateZeta(stack, self))
			ci.cancel();
	}
}
