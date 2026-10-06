package org.violetmoon.quark.gametest;

import org.violetmoon.zeta.util.ZetaTags;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootDataType;

/**
 * Checks that Quark's data converted from Forge (resource conditions, c tags, loot tables) loads on Fabric.
 */
public class QuarkDataTests implements FabricGameTest {

	@GameTest(template = EMPTY_STRUCTURE)
	public void recipesLoad(GameTestHelper helper) {
		RecipeManager recipes = helper.getLevel().getRecipeManager();
		long quarkRecipes = recipes.getRecipeIds().filter(id -> id.getNamespace().equals("quark")).count();
		helper.assertTrue(quarkRecipes > 1000, "only " + quarkRecipes + " quark recipes loaded");

		// fabric:and / fabric:not / fabric:all_mods_loaded / quark:flag (sturdy stone module is on by default)
		helper.assertTrue(recipes.byKey(new ResourceLocation("quark", "building/crafting/sturdy_stone")).isPresent(), "sturdy stone recipe missing");

		// quark:flag alone, plus Forge's stack result for cooking recipes
		var boneMeal = recipes.byKey(new ResourceLocation("quark", "tweaks/smelting/bone_meal_utility"));
		helper.assertTrue(boneMeal.isPresent(), "bone meal smelting recipe missing");
		int count = boneMeal.get().getResultItem(helper.getLevel().registryAccess()).getCount();
		helper.assertTrue(count == 3, "bone meal smelting gives " + count + ", expected 3");
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void tagsLoad(GameTestHelper helper) {
		helper.assertTrue(Items.CHEST.builtInRegistryHolder().is(ZetaTags.Items.CHESTS_WOODEN), "minecraft:chest not in c:wooden_chests");
		helper.assertTrue(Items.STICK.builtInRegistryHolder().is(itemTag("wooden_rods")), "minecraft:stick not in c:wooden_rods");
		Item jasper = BuiltInRegistries.ITEM.get(new ResourceLocation("quark", "jasper"));
		helper.assertTrue(jasper.builtInRegistryHolder().is(itemTag("stone")), "quark:jasper not in c:stone");
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void lootTablesLoad(GameTestHelper helper) {
		var loot = helper.getLevel().getServer().getLootData();
		long quarkTables = loot.getKeys(LootDataType.TABLE).stream().filter(id -> id.getNamespace().equals("quark")).count();
		helper.assertTrue(quarkTables > 100, "only " + quarkTables + " quark loot tables loaded");
		helper.succeed();
	}

	private static TagKey<Item> itemTag(String name) {
		return TagKey.create(Registries.ITEM, new ResourceLocation("c", name));
	}
}
