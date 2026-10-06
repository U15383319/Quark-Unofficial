package org.violetmoon.zeta.util;

import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fabric stand-in for Forge's IPlantable / PlantType and the default IForgeBlock#canSustainPlant, reproducing the
 * plant types Forge assigns to the vanilla plants.
 */
public final class ZetaPlantable {

	public static final String PLAINS = "plains", DESERT = "desert", BEACH = "beach", CAVE = "cave", WATER = "water", NETHER = "nether", CROP = "crop";

	private ZetaPlantable() { }

	/**
	 * Forge: block instanceof IPlantable.
	 */
	public static boolean isPlantable(Block block) {
		return block instanceof BushBlock || block instanceof CactusBlock || block instanceof SugarCaneBlock
			|| block instanceof BambooSaplingBlock || block instanceof BambooStalkBlock;
	}

	/**
	 * Forge: IPlantable#getPlantType, lowercase like PlantType#getName.
	 */
	public static String getPlantType(Block block) {
		if(block instanceof NetherWartBlock)
			return NETHER;
		if(block instanceof CropBlock)
			return CROP;
		if(block instanceof DeadBushBlock || block instanceof CactusBlock)
			return DESERT;
		if(block instanceof WaterlilyBlock)
			return WATER;
		if(block == Blocks.RED_MUSHROOM || block == Blocks.BROWN_MUSHROOM)
			return CAVE;
		if(block instanceof SugarCaneBlock)
			return BEACH;
		return PLAINS;
	}

	/**
	 * Forge: IForgeBlock#canSustainPlant with its default (vanilla) behavior; Zeta blocks answer themselves.
	 */
	public static boolean canSustainPlant(BlockState ground, BlockGetter level, BlockPos pos, Direction facing, Block plant) {
		if(ground.getBlock() instanceof IZetaBlockExtensions ext)
			return ext.canSustainPlantZeta(ground, level, pos, facing, getPlantType(plant));

		if(plant == Blocks.CACTUS)
			return ground.is(Blocks.CACTUS) || ground.is(BlockTags.SAND);
		if(plant == Blocks.SUGAR_CANE && ground.is(Blocks.SUGAR_CANE))
			return true;

		String type = getPlantType(plant);
		return switch(type) {
			case DESERT -> ground.is(BlockTags.SAND) || ground.is(BlockTags.TERRACOTTA);
			case NETHER -> ground.is(Blocks.SOUL_SAND);
			case CROP -> ground.getBlock() instanceof FarmBlock;
			case CAVE -> ground.isFaceSturdy(level, pos, Direction.UP);
			case WATER -> level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).isEmpty();
			case BEACH -> {
				boolean soil = ground.is(BlockTags.DIRT) || ground.is(BlockTags.SAND);
				boolean water = false;
				for(Direction dir : Direction.Plane.HORIZONTAL)
					water |= level.getFluidState(pos.relative(dir)).is(FluidTags.WATER) || level.getBlockState(pos.relative(dir)).is(Blocks.FROSTED_ICE);
				yield soil && water;
			}
			default -> ground.is(BlockTags.DIRT) || ground.getBlock() instanceof FarmBlock;
		};
	}
}
