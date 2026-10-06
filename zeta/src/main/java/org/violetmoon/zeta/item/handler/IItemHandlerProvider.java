package org.violetmoon.zeta.item.handler;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

/**
 * Replaces Forge's getCapability(ForgeCapabilities.ITEM_HANDLER, side) on block entities.
 * {@link ItemHandlers} looks for this first, and also exposes it to Fabric's ItemStorage.SIDED.
 */
public interface IItemHandlerProvider {
	@Nullable
	IItemHandler getItemHandler(@Nullable Direction side);
}
