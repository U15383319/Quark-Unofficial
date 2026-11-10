package org.violetmoon.zetaimplfabric.ext;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Forge's BlockBehaviour.Properties#lootFrom, injected with Loom interface injection and implemented by
 * BlockPropertiesExtMixin. Unlike vanilla's dropsLike it reads the other block's loot table only when it is first
 * needed, so it works with a block that isn't registered yet (dropsLike would read and cache "blocks/air").
 */
public interface IZetaBlockPropertiesExtensions {

	default BlockBehaviour.Properties lootFrom(Supplier<? extends Block> block) {
		throw new AssertionError("implemented by BlockPropertiesExtMixin");
	}

	default @Nullable Supplier<? extends Block> zeta$getLootFrom() {
		throw new AssertionError("implemented by BlockPropertiesExtMixin");
	}
}
