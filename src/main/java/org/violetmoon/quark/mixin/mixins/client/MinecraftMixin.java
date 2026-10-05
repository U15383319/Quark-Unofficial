package org.violetmoon.quark.mixin.mixins.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.client.module.LongRangePickBlockModule;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow
	@Nullable
	public HitResult hitResult;

	@Shadow @javax.annotation.Nullable public LocalPlayer player;

	@Inject(method = "pickBlock", at = @At("HEAD"))
	private void pickBlockHead(CallbackInfo ci, @Share("hitResult") LocalRef<HitResult> hitResult) {
		Minecraft self = (Minecraft) (Object) this;
		hitResult.set(self.hitResult);
		self.hitResult = Quark.ZETA.modules.get(LongRangePickBlockModule.class).transformHitResult(self.hitResult);
	}

	@Inject(method = "pickBlock", at = @At("RETURN"))
	private void pickBlockReturn(CallbackInfo ci, @Share("hitResult") LocalRef<HitResult> hitResult) {
		Minecraft self = (Minecraft) (Object) this;
		self.hitResult = hitResult.get();
	}

	@Inject(method = "pickBlock", cancellable = true,  at = @At(value = "INVOKE",
			shift = At.Shift.BEFORE,
			target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I"))
	private void pickBlockPick(CallbackInfo ci, @Local LocalRef<ItemStack> stack) {
		ItemStack oldPick = stack.get();
		ItemStack newPick = VariantSelectorModule.Client.onPickBlock(player, oldPick);
		 if(newPick == oldPick){
			 //cancel pick. variant selector has picked
			 ci.cancel();
		}else if(newPick != null){
			 //pick this instead
			 stack.set(newPick);
		}
	}


}
