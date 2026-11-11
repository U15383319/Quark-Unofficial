package org.violetmoon.quark.gametest;

import java.util.Locale;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Zeta's biome modifier (Forge: a biome modifier on every biome): Quark mob spawns and the deferred worldgen features
 * that carry all of Quark's world generation.
 */
public class QuarkWorldTests implements FabricGameTest {

	@GameTest(template = EMPTY_STRUCTURE)
	public void mobsSpawnInTheirBiomes(GameTestHelper helper) {
		assertSpawns(helper, Biomes.PLAINS, "toretoise", MobCategory.MONSTER, true);
		assertSpawns(helper, Biomes.PLAINS, "stoneling", MobCategory.MONSTER, true);
		assertSpawns(helper, Biomes.BEACH, "crab", MobCategory.CREATURE, true);
		assertSpawns(helper, Biomes.NETHER_WASTES, "foxhound", MobCategory.MONSTER, true);
		assertSpawns(helper, Biomes.SOUL_SAND_VALLEY, "wraith", MobCategory.MONSTER, true);
		// biome blacklists / whitelists
		assertSpawns(helper, Biomes.NETHER_WASTES, "toretoise", MobCategory.MONSTER, false);
		assertSpawns(helper, Biomes.PLAINS, "crab", MobCategory.CREATURE, false);

		// CostSensitiveEntitySpawnConfig: wraiths get a spawn cost like on Forge
		EntityType<?> wraith = quarkEntity("wraith");
		helper.assertTrue(biome(helper, Biomes.SOUL_SAND_VALLEY).value().getMobSettings().getMobSpawnCost(wraith) != null, "wraith has no spawn cost");
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void deferredWorldgenInEveryBiome(GameTestHelper helper) {
		for(ResourceKey<Biome> key : java.util.List.of(Biomes.PLAINS, Biomes.FROZEN_PEAKS, Biomes.NETHER_WASTES, Biomes.END_HIGHLANDS)) {
			var settings = biome(helper, key).value().getGenerationSettings();
			for(GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
				ResourceLocation id = new ResourceLocation("zeta", "deferred_feature_" + step.name().toLowerCase(Locale.ROOT));
				var features = settings.features();
				boolean found = step.ordinal() < features.size() && features.get(step.ordinal()).stream()
					.anyMatch(h -> h.unwrapKey().map(k -> k.location().equals(id)).orElse(false));
				helper.assertTrue(found, key.location() + " has no " + id + " at " + step);
			}
		}
		helper.succeed();
	}

	private static void assertSpawns(GameTestHelper helper, ResourceKey<Biome> key, String mob, MobCategory category, boolean expected) {
		EntityType<?> type = quarkEntity(mob);
		boolean spawns = biome(helper, key).value().getMobSettings().getMobs(category).unwrap().stream().anyMatch(e -> e.type == type);
		helper.assertTrue(spawns == expected, "quark:" + mob + (expected ? " doesn't spawn" : " spawns") + " in " + key.location());
	}

	private static Holder<Biome> biome(GameTestHelper helper, ResourceKey<Biome> key) {
		return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
	}

	private static EntityType<?> quarkEntity(String name) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(new ResourceLocation("quark", name)).orElseThrow();
	}
}
