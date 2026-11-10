package org.violetmoon.quark.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;

final class QuarkTestUtil {

	private QuarkTestUtil() { }

	static Block block(String name) {
		Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation("quark", name));
		if(block == Blocks.AIR)
			throw new IllegalStateException("no block quark:" + name);
		return block;
	}

	static Item item(String name) {
		Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("quark", name));
		if(item == Items.AIR)
			throw new IllegalStateException("no item quark:" + name);
		return item;
	}

	static int count(Container container, Item item) {
		int count = 0;
		for(int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if(stack.is(item))
				count += stack.getCount();
		}
		return count;
	}
}
