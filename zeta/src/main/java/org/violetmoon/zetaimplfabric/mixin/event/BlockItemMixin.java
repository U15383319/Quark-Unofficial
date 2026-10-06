package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

	// Forge BlockEvent.EntityPlaceEvent (server side)
	@Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"))
	private void zeta$entityPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir,
								  @Local BlockPos pos, @Local Level level, @Local(ordinal = 1) BlockState placed) {
		if(!level.isClientSide)
			FabricZWorldEvents.fireEntityPlace(level, pos, placed);
	}
}
