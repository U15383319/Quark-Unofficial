package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge's FireBlock asks the block: canCatchFire -> isFlammable, tryCatchFire -> getFlammability, and the ignite odds
 * of the neighbours -> getFireSpreadSpeed. (Zeta's hooks ignore the face, which vanilla doesn't track here.)
 */
@Mixin(FireBlock.class)
public class FireBlockHookMixin {

	@Inject(method = "canBurn", at = @At("HEAD"), cancellable = true)
	private void zeta$isFlammable(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		IZetaBlockExtensions ext = ZetaHooks.block(state, "isFlammableZeta");
		if(ext == null)
			ext = ZetaHooks.block(state, "getFlammabilityZeta"); // isFlammable defaults to flammability > 0
		if(ext != null)
			cir.setReturnValue(ext.isFlammableZeta(state, null, null, null));
	}

	@WrapOperation(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;getBurnOdds(Lnet/minecraft/world/level/block/state/BlockState;)I"))
	private int zeta$flammability(FireBlock fire, BlockState state, Operation<Integer> original, Level level, BlockPos pos, int chance, RandomSource random, int age) {
		IZetaBlockExtensions ext = ZetaHooks.block(state, "getFlammabilityZeta");
		return ext != null ? ext.getFlammabilityZeta(state, level, pos, null) : original.call(fire, state);
	}

	@WrapOperation(method = "getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FireBlock;getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I"))
	private int zeta$fireSpreadSpeed(FireBlock fire, BlockState state, Operation<Integer> original, LevelReader level, BlockPos pos) {
		IZetaBlockExtensions ext = ZetaHooks.block(state, "getFireSpreadSpeedZeta");
		return ext != null ? ext.getFireSpreadSpeedZeta(state, level, null, null) : original.call(fire, state);
	}
}
