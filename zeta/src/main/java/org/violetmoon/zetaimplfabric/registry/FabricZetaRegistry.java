package org.violetmoon.zetaimplfabric.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.violetmoon.zeta.registry.ZetaRegistry;
import org.violetmoon.zetaimplfabric.FabricZeta;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Forge registers deferred entries in RegisterEvent, one registry at a time. Fabric registries are open during mod
 * initialization, so everything is flushed straight into BuiltInRegistries right after ZRegister is fired.
 */
public class FabricZetaRegistry extends ZetaRegistry {

	// Forge fires RegisterEvent for blocks, fluids and items first; everything else follows in vanilla registry order
	private static final List<ResourceLocation> PRIORITY = List.of(
		Registries.BLOCK.location(),
		Registries.FLUID.location(),
		Registries.ITEM.location()
	);

	public FabricZetaRegistry(FabricZeta z) {
		super(z);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void performRegistration() {
		Set<ResourceLocation> order = new LinkedHashSet<>(PRIORITY);
		for(ResourceLocation id : BuiltInRegistries.REGISTRY.keySet())
			order.add(id);

		for(ResourceLocation registryRes : order) {
			Collection<Supplier<Object>> ourEntries = getDefers(registryRes);
			if(ourEntries == null || ourEntries.isEmpty())
				continue;

			Registry<Object> registry = (Registry<Object>) BuiltInRegistries.REGISTRY.get(registryRes);
			if(registry == null)
				throw new IllegalStateException("No such registry " + registryRes);

			ResourceKey<Registry<Object>> keyGeneric = ResourceKey.createRegistryKey(registryRes);

			// copy; creating block items can't add new defers to the list we're iterating, but be safe
			for(Supplier<Object> supplier : new ArrayList<>(ourEntries)) {
				Object entry = supplier.get();
				ResourceLocation name = internalNames.get(entry);
				z.log.debug("Registering to {} - {}", registryRes, name);
				Registry.register(registry, name, entry);
				trackRegisteredObject(keyGeneric, (net.minecraft.core.Holder) registry.wrapAsHolder(entry));
			}

			clearDeferCache(registryRes);
		}
	}
}
