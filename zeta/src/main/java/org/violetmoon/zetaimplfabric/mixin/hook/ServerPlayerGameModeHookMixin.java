package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Forge: sneaking doesn't skip the block's use action when both held items say so (IForgeItem#doesSneakBypassUse).
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeHookMixin {

	@ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
	private boolean zeta$sneakBypassUse(boolean sneaking, ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit) {
		return sneaking && !ZetaHooks.sneakBypassesUse(player, level, hit.getBlockPos());
	}
}
