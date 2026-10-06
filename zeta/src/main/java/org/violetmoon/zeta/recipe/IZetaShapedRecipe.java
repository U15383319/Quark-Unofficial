package org.violetmoon.zeta.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Fabric stand-in for Forge's IShapedRecipe: lets the recipe book lay out non-ShapedRecipe recipes as a grid.
 */
public interface IZetaShapedRecipe<C extends Container> extends Recipe<C> {
	int getRecipeWidth();

	int getRecipeHeight();
}
