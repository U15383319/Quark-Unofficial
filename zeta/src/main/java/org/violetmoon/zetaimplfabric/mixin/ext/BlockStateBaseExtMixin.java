package org.violetmoon.zetaimplfabric.mixin.ext;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.zetaimplfabric.ext.IZetaBlockStateExtensions;

import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseExtMixin implements IZetaBlockStateExtensions {

	@Shadow
	public abstract Block getBlock();

	@Shadow
	protected abstract BlockState asState();

	// Forge: the block can decide its push reaction per state
	@Inject(method = "getPistonPushReaction", at = @At("HEAD"), cancellable = true)
	private void zeta$pushReaction(CallbackInfoReturnable<PushReaction> cir) {
		PushReaction reaction = getBlock().getPistonPushReaction(asState());
		if(reaction != null)
			cir.setReturnValue(reaction);
	}
}
