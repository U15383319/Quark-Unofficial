package org.violetmoon.zetaimplfabric.ext.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Forge: ChestRenderer#getMaterial, injected with Loom interface injection; null means "vanilla material".
 * Read by ChestRendererMixin.
 */
public interface IZetaChestRendererExtensions<T extends BlockEntity> {
	default @Nullable Material getMaterial(T blockEntity, ChestType chestType) {
		return null;
	}
}
