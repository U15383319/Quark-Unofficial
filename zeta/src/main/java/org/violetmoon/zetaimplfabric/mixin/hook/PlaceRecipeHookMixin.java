package org.violetmoon.zetaimplfabric.mixin.hook;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.violetmoon.zeta.recipe.IZetaShapedRecipe;

import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Forge PlaceRecipe: the recipe book lays out any IShapedRecipe as a grid, not only ShapedRecipe (IZetaShapedRecipe).
 */
@Mixin(PlaceRecipe.class)
public interface PlaceRecipeHookMixin<T> {

	@ModifyVariable(method = "placeRecipe", at = @At(value = "STORE", ordinal = 0), index = 7)
	private int zeta$recipeWidth(int width, int gridWidth, int gridHeight, int outputSlot, Recipe<?> recipe, Iterator<T> ingredients, int maxAmount) {
		return recipe instanceof IZetaShapedRecipe<?> shaped ? shaped.getRecipeWidth() : width;
	}

	@ModifyVariable(method = "placeRecipe", at = @At(value = "STORE", ordinal = 0), index = 8)
	private int zeta$recipeHeight(int height, int gridWidth, int gridHeight, int outputSlot, Recipe<?> recipe, Iterator<T> ingredients, int maxAmount) {
		return recipe instanceof IZetaShapedRecipe<?> shaped ? shaped.getRecipeHeight() : height;
	}
}
