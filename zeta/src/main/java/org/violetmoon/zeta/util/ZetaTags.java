package org.violetmoon.zeta.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * Fabric stand-in for the parts of Forge's net.minecraftforge.common.Tags that Quark uses.
 * <p>
 * The Fabric ecosystem shares tags under the "c" namespace. Where Fabric API defines an equivalent conventional tag it
 * is used; otherwise a "c" tag with the same contents as Forge's is shipped in Quark's data (see data/c/tags).
 */
public final class ZetaTags {

	private ZetaTags() { }

	public static final class Blocks {
		public static final TagKey<Block> CHESTS = tag("chests"); // Fabric API
		public static final TagKey<Block> TRAPPED_CHESTS = tag("trapped_chests");
		public static final TagKey<Block> STONE = tag("stone");
		public static final TagKey<Block> ORES_DIAMOND = tag("diamond_ores");

		private static TagKey<Block> tag(String name) {
			return TagKey.create(Registries.BLOCK, new ResourceLocation("c", name));
		}
	}

	public static final class Items {
		public static final TagKey<Item> CHESTS_WOODEN = tag("wooden_chests");
		public static final TagKey<Item> GEMS_DIAMOND = tag("diamonds"); // Fabric API
		public static final TagKey<Item> GEMS_LAPIS = tag("lapis"); // Fabric API

		private static TagKey<Item> tag(String name) {
			return TagKey.create(Registries.ITEM, new ResourceLocation("c", name));
		}
	}

	public static final class Biomes {
		public static final TagKey<Biome> IS_PLAINS = tag("plains"); // Fabric API
		public static final TagKey<Biome> IS_SANDY = tag("sandy");
		public static final TagKey<Biome> IS_SNOWY = tag("snowy"); // Fabric API
		public static final TagKey<Biome> IS_SWAMP = tag("swamp"); // Fabric API
		public static final TagKey<Biome> IS_VOID = tag("void"); // Fabric API

		private static TagKey<Biome> tag(String name) {
			return TagKey.create(Registries.BIOME, new ResourceLocation("c", name));
		}
	}
}
