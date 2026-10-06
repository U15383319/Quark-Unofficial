package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge: the beam colour comes from IForgeBlock#getBeaconColorMultiplier (any block, any colour).
 * <p>
 * Vanilla only colours the beam for BeaconBeamBlocks and casts to that, so for a Zeta block with a colour the loop is
 * handed a stained glass block, whose colour is then swapped for the Zeta one.
 */
@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityHookMixin {

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private static BlockState zeta$capturePos(Level level, BlockPos pos, Operation<BlockState> original, @Share("pos") LocalRef<BlockPos> sharedPos) {
		sharedPos.set(pos);
		return original.call(level, pos);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
	private static Block zeta$beaconColor(BlockState state, Operation<Block> original, Level level, BlockPos beaconPos, BlockState beaconState, BeaconBlockEntity beacon,
										  @Share("pos") LocalRef<BlockPos> sharedPos, @Share("color") LocalRef<float[]> color) {
		Block block = original.call(state);
		color.set(null);
		IZetaBlockExtensions ext = ZetaHooks.block(state, "getBeaconColorMultiplierZeta");
		if(ext == null)
			return block;

		float[] multiplier = ext.getBeaconColorMultiplierZeta(state, level, sharedPos.get(), beaconPos);
		if(multiplier == null)
			return block instanceof BeaconBeamBlock ? Blocks.STONE : block; // Forge: no colour means no beam segment
		color.set(multiplier);
		return Blocks.WHITE_STAINED_GLASS;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColors()[F"))
	private static float[] zeta$useColor(DyeColor dye, Operation<float[]> original, @Share("color") LocalRef<float[]> color) {
		float[] multiplier = color.get();
		return multiplier != null ? multiplier : original.call(dye);
	}
}
