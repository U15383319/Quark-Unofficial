package org.violetmoon.quark.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.building.module.ShearVinesModule;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	public void getDestroySpeed(ItemStack pStack, BlockState pState, CallbackInfoReturnable<Float> cbi) {
		if(pState.is(ShearVinesModule.cut_vine))
			cbi.setReturnValue(2F);
	}
	
}
