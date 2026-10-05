package org.violetmoon.quark.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IMagnetMoveAction {

	// These two should likely have that tile param replaced with tile pos

	void onMagnetMoved(Level world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile);

	default boolean canMagnetMove(Level world, BlockPos pos, Direction direction, BlockState state, BlockEntity tile){
		return true;
	}
}
