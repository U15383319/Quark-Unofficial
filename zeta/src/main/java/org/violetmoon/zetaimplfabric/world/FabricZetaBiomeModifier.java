package org.violetmoon.zetaimplfabric.world;

import java.util.Locale;

import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.config.type.CostSensitiveEntitySpawnConfig;
import org.violetmoon.zeta.mod.ZetaMod;
import org.violetmoon.zeta.util.zetalist.ZetaList;
import org.violetmoon.zeta.world.EntitySpawnHandler.TrackedSpawnConfig;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Fabric version of zetaimplforge's ZetaBiomeModifier + ZetaSpawnModifier (a Forge biome modifier applied to every
 * biome): adds Zeta's deferred worldgen feature to every generation step, and every Zeta mod's tracked mob spawns.
 * <p>
 * Fabric biome modifications need registered features, so the deferred features have configured/placed feature files
 * in data/zeta/worldgen (WorldGenHandler registers the Feature type itself).
 */
public final class FabricZetaBiomeModifier {

	private FabricZetaBiomeModifier() { }

	public static void init() {
		BiomeModifications.create(new ResourceLocation(ZetaMod.ZETA_ID, "biome_modifier"))
			.add(ModificationPhase.ADDITIONS, BiomeSelectors.all(), FabricZetaBiomeModifier::modify);
	}

	private static void modify(BiomeSelectionContext selection, BiomeModificationContext context) {
		for(GenerationStep.Decoration step : GenerationStep.Decoration.values())
			context.getGenerationSettings().addFeature(step, ResourceKey.create(Registries.PLACED_FEATURE,
				new ResourceLocation(ZetaMod.ZETA_ID, "deferred_feature_" + step.name().toLowerCase(Locale.ROOT))));

		BiomeModificationContext.SpawnSettingsContext spawns = context.getSpawnSettings();
		for(Zeta zeta : ZetaList.INSTANCE.getZetas()) {
			for(TrackedSpawnConfig c : zeta.entitySpawn.trackedSpawnConfigs) {
				if(!c.secondary)
					spawns.removeSpawnsOfEntityType(c.entityType);

				if(c.config.isEnabled() && c.config.biomes.canSpawn(selection.getBiomeRegistryEntry()))
					spawns.addSpawn(c.classification, c.getEntry());

				// Forge MobSpawnSettingsBuilder#addMobCharge(type, charge, energyBudget)
				if(c.config instanceof CostSensitiveEntitySpawnConfig csc)
					spawns.setSpawnCost(c.entityType, csc.spawnCost, csc.maxCost);
			}
		}
	}
}
