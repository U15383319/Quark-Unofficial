package org.violetmoon.zeta.registry;

import org.violetmoon.zeta.recipe.IZetaCondition;
import org.violetmoon.zeta.recipe.IZetaConditionSerializer;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

public interface CraftingExtensionsRegistry {
	<T extends IZetaCondition> IZetaConditionSerializer<T> registerConditionSerializer(IZetaConditionSerializer<T> serializer);

	// Fabric: custom ingredients go through Fabric's recipe API (vanilla Ingredient is final)
	<T extends CustomIngredient> CustomIngredientSerializer<T> registerIngredientSerializer(CustomIngredientSerializer<T> serializer);
}
