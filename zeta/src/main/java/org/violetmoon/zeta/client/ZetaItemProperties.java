package org.violetmoon.zeta.client;

import java.util.HashMap;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Fabric: vanilla's ItemProperties#register only takes clamped functions; Forge's took any ItemPropertyFunction and
 * didn't clamp, which Quark relies on (e.g. bucket variant indices). PROPERTIES is widened.
 */
public final class ZetaItemProperties {

	private ZetaItemProperties() { }

	public static ItemPropertyFunction register(Item item, ResourceLocation name, ItemPropertyFunction property) {
		ItemProperties.PROPERTIES.computeIfAbsent(item, k -> new HashMap<>()).put(name, property);
		return property;
	}
}
