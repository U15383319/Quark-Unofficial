package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge ForgeHooks#isLivingOnLadder: the block decides whether it is a ladder (IForgeBlock#isLadder).
 */
@Mixin(LivingEntity.class)
public class LivingEntityHookMixin {

	@WrapOperation(method = "onClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
	private boolean zeta$isLadder(BlockState state, TagKey<Block> climbable, Operation<Boolean> original) {
		IZetaBlockExtensions ext = ZetaHooks.block(state, "isLadderZeta");
		if(ext == null)
			return original.call(state, climbable);
		LivingEntity self = (LivingEntity) (Object) this;
		return ext.isLadderZeta(state, self.level(), self.blockPosition(), self);
	}
}
