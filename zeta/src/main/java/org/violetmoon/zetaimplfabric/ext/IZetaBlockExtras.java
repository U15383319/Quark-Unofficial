package org.violetmoon.zetaimplfabric.ext;

import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Forge IForgeBlock methods Quark calls on Block, injected with Loom interface injection.
 */
public interface IZetaBlockExtras {

	default SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
		return IZetaBlockExtensions.get(state).getSoundTypeZeta(state, level, pos, entity);
	}

	/**
	 * Forge: IForgeBlock#getPistonPushReaction; null means "use the block properties". Read by BlockStateBaseExtMixin.
	 */
	default @Nullable PushReaction getPistonPushReaction(BlockState state) {
		return null;
	}

	/**
	 * Forge's default: ForgeHooks#isCorrectToolForDrops.
	 */
	default boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
		return !state.requiresCorrectToolForDrops() || player.hasCorrectToolForDrops(state);
	}
}
