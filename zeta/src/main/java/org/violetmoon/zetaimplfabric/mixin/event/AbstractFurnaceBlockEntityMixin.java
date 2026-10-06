package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge ForgeHooks#getBurnTime: per-item burn time (Zeta items) and FurnaceFuelBurnTimeEvent.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

	// Forge keeps the constructor's recipe type in a field; vanilla only hands it to the recipe cache
	@Unique private RecipeType<? extends AbstractCookingRecipe> recipeType;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void zeta$captureRecipeType(BlockEntityType<?> type, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
		this.recipeType = recipeType;
	}

	@Inject(method = "getBurnDuration", at = @At("HEAD"), cancellable = true)
	private void zeta$burnDuration(ItemStack fuel, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(FabricZWorldEvents.getBurnTime(fuel, recipeType));
	}

	@Inject(method = "isFuel", at = @At("HEAD"), cancellable = true)
	private static void zeta$isFuel(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(FabricZWorldEvents.getBurnTime(stack, null) > 0);
	}
}
