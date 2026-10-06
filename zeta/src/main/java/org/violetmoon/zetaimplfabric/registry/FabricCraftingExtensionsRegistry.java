package org.violetmoon.zetaimplfabric.registry;

import java.util.Collection;
import java.util.Map;

import org.violetmoon.zeta.recipe.IZetaCondition;
import org.violetmoon.zeta.recipe.IZetaConditionSerializer;
import org.violetmoon.zeta.registry.CraftingExtensionsRegistry;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Forge recipe conditions -> Fabric resource conditions ("fabric:load_conditions" in JSON),
 * Forge ingredient serializers -> Fabric custom ingredients ("fabric:type" in JSON).
 */
public class FabricCraftingExtensionsRegistry implements CraftingExtensionsRegistry {

	// Zeta's conditions (flags) never look at tags; Fabric's condition predicates don't get a tag context either
	private static final IZetaCondition.IContext EMPTY_CONTEXT = new IZetaCondition.IContext() {
		@Override
		public <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
			return Map.of();
		}
	};

	@Override
	public <T extends IZetaCondition> IZetaConditionSerializer<T> registerConditionSerializer(IZetaConditionSerializer<T> serializer) {
		ResourceConditions.register(serializer.getID(), json -> serializer.read(json).test(EMPTY_CONTEXT));
		return serializer;
	}

	@Override
	public <T extends CustomIngredient> CustomIngredientSerializer<T> registerIngredientSerializer(CustomIngredientSerializer<T> serializer) {
		CustomIngredientSerializer.register(serializer);
		return serializer;
	}
}
