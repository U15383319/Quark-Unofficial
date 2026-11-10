package org.violetmoon.zetaimplfabric.mixin.ext;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.violetmoon.zetaimplfabric.ext.IZetaBlockPropertiesExtensions;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(BlockBehaviour.Properties.class)
public class BlockPropertiesExtMixin implements IZetaBlockPropertiesExtensions {

	@Unique private @Nullable Supplier<? extends Block> zeta$lootFrom;

	@Override
	public BlockBehaviour.Properties lootFrom(Supplier<? extends Block> block) {
		zeta$lootFrom = block;
		return (BlockBehaviour.Properties) (Object) this;
	}

	@Override
	public @Nullable Supplier<? extends Block> zeta$getLootFrom() {
		return zeta$lootFrom;
	}
}
