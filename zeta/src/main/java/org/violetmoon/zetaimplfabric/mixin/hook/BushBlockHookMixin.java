package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zeta.util.ZetaPlantable;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge BushBlock#canSurvive: a placed plant asks the soil (IForgeBlock#canSustainPlant).
 */
@Mixin(BushBlock.class)
public class BushBlockHookMixin {

	@ModifyReturnValue(method = "canSurvive", at = @At("RETURN"))
	private boolean zeta$canSustainPlant(boolean original, BlockState state, LevelReader level, BlockPos pos) {
		if(state.getBlock() != (Object) this)
			return original;
		BlockPos soilPos = pos.below();
		BlockState soil = level.getBlockState(soilPos);
		IZetaBlockExtensions ext = ZetaHooks.block(soil, "canSustainPlantZeta");
		return ext != null ? ext.canSustainPlantZeta(soil, level, soilPos, Direction.UP, ZetaPlantable.getPlantType(state.getBlock())) : original;
	}
}
