package org.violetmoon.zetaimplfabric.ext;

import net.minecraft.nbt.CompoundTag;

/**
 * Forge IForgeBlockEntity#getPersistentData, injected onto BlockEntity and saved as "ForgeData" like on Forge.
 */
public interface IZetaBlockEntityExtensions {

	/**
	 * Forge: BlockEntity#serializeNBT.
	 */
	default CompoundTag serializeNBT() {
		return ((net.minecraft.world.level.block.entity.BlockEntity) this).saveWithFullMetadata();
	}

	default CompoundTag getPersistentData() {
		throw new AssertionError("implemented by BlockEntityMixin");
	}
}
