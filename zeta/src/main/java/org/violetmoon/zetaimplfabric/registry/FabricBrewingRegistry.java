package org.violetmoon.zetaimplfabric.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.registry.BrewingRegistry;
import org.violetmoon.zetaimplfabric.FabricZeta;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Forge added an Ingredient-based PotionBrewing.Mix; Fabric API provides FabricBrewingRecipeRegistry for the same thing.
 */
public class FabricBrewingRegistry extends BrewingRegistry {
	public FabricBrewingRegistry(FabricZeta zeta) {
		super(zeta);
	}

	private record DelayedPotion(Potion input, Supplier<Ingredient> reagentSupplier, Potion output) {
		void register() {
			FabricBrewingRecipeRegistry.registerPotionRecipe(input, reagentSupplier.get(), output);
		}
	}

	private List<DelayedPotion> delayedPotions = new ArrayList<>();
	private boolean okToRegisterImmediately = false;

	@Override
	public void addBrewingRecipe(Potion input, Supplier<Ingredient> reagentSupplier, Potion output) {
		DelayedPotion d = new DelayedPotion(input, reagentSupplier, output);

		if(okToRegisterImmediately)
			d.register();
		else
			delayedPotions.add(d);
	}

	@LoadEvent
	public void commonSetup(ZCommonSetup event) {
		event.enqueueWork(() -> {
			okToRegisterImmediately = true;
			delayedPotions.forEach(DelayedPotion::register);
			delayedPotions = null;
		});
	}
}
