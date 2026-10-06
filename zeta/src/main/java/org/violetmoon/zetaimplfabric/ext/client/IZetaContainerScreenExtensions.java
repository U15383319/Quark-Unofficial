package org.violetmoon.zetaimplfabric.ext.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.inventory.Slot;

/**
 * Forge's AbstractContainerScreen getters, injected with Loom interface injection.
 */
public interface IZetaContainerScreenExtensions {
	default int getGuiLeft() {
		throw new AssertionError("implemented by AbstractContainerScreenExtMixin");
	}

	default int getGuiTop() {
		throw new AssertionError("implemented by AbstractContainerScreenExtMixin");
	}

	default int getXSize() {
		throw new AssertionError("implemented by AbstractContainerScreenExtMixin");
	}

	default int getYSize() {
		throw new AssertionError("implemented by AbstractContainerScreenExtMixin");
	}

	default @Nullable Slot getSlotUnderMouse() {
		throw new AssertionError("implemented by AbstractContainerScreenExtMixin");
	}
}
