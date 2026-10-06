package org.violetmoon.zetaimplfabric.mixin.hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge: a bookshelf position counts when the block has an enchanting power (IForgeBlock#getEnchantPowerBonus).
 */
@Mixin(EnchantmentTableBlock.class)
public class EnchantmentTableBlockHookMixin {

	@WrapOperation(method = "isValidBookShelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
	private static boolean zeta$enchantPower(BlockState state, TagKey<Block> powerProvider, Operation<Boolean> original, Level level, BlockPos tablePos, BlockPos offset) {
		IZetaBlockExtensions ext = ZetaHooks.block(state, "getEnchantPowerBonusZeta");
		return ext != null ? ext.getEnchantPowerBonusZeta(state, level, tablePos.offset(offset)) != 0 : original.call(state, powerProvider);
	}
}
