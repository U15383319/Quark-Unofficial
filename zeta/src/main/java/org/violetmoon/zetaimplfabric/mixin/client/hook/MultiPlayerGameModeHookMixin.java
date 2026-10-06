package org.violetmoon.zetaimplfabric.mixin.client.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Client side of ServerPlayerGameModeHookMixin (IForgeItem#doesSneakBypassUse).
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeHookMixin {

	@ModifyExpressionValue(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"))
	private boolean zeta$sneakBypassUse(boolean sneaking, LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
		return sneaking && !ZetaHooks.sneakBypassesUse(player, player.level(), hit.getBlockPos());
	}
}
