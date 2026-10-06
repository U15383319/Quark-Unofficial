package org.violetmoon.zetaimplfabric.ext;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Forge IForgeBlockState methods Quark calls, injected onto BlockStateBase (Loom interface injection) and
 * answered by the block's IZetaBlockExtensions, which is where Forge would have dispatched them too.
 */
public interface IZetaBlockStateExtensions {

	private BlockState zeta$self() {
		return (BlockState) this;
	}

	default float getEnchantPowerBonus(LevelReader level, BlockPos pos) {
		return IZetaBlockExtensions.get(zeta$self()).getEnchantPowerBonusZeta(zeta$self(), level, pos);
	}

	default SoundType getSoundType(LevelReader level, BlockPos pos, @Nullable Entity entity) {
		return IZetaBlockExtensions.get(zeta$self()).getSoundTypeZeta(zeta$self(), level, pos, entity);
	}

	default int getLightEmission(BlockGetter level, BlockPos pos) {
		return IZetaBlockExtensions.get(zeta$self()).getLightEmissionZeta(zeta$self(), level, pos);
	}

	default boolean isStickyBlock() {
		return IZetaBlockExtensions.get(zeta$self()).isStickyBlockZeta(zeta$self());
	}

	default boolean canStickTo(BlockState other) {
		return IZetaBlockExtensions.get(zeta$self()).canStickToZeta(zeta$self(), other);
	}

	default boolean isLadder(LevelReader level, BlockPos pos, LivingEntity entity) {
		return IZetaBlockExtensions.get(zeta$self()).isLadderZeta(zeta$self(), level, pos, entity);
	}

	default boolean isScaffolding(LivingEntity entity) {
		return IZetaBlockExtensions.get(zeta$self()).isScaffoldingZeta(zeta$self(), entity.level(), entity.blockPosition(), entity);
	}

	default @Nullable float[] getBeaconColorMultiplier(LevelReader level, BlockPos pos, BlockPos beaconPos) {
		return IZetaBlockExtensions.get(zeta$self()).getBeaconColorMultiplierZeta(zeta$self(), level, pos, beaconPos);
	}

	default boolean isFlammable(BlockGetter level, BlockPos pos, Direction face) {
		return IZetaBlockExtensions.get(zeta$self()).isFlammableZeta(zeta$self(), level, pos, face);
	}

	default int getFlammability(BlockGetter level, BlockPos pos, Direction face) {
		return IZetaBlockExtensions.get(zeta$self()).getFlammabilityZeta(zeta$self(), level, pos, face);
	}

	default int getFireSpreadSpeed(BlockGetter level, BlockPos pos, Direction face) {
		return IZetaBlockExtensions.get(zeta$self()).getFireSpreadSpeedZeta(zeta$self(), level, pos, face);
	}
}
