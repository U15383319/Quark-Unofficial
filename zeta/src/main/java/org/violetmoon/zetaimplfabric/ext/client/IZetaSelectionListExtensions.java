package org.violetmoon.zetaimplfabric.ext.client;

/**
 * Forge's AbstractSelectionList bounds getters, injected with Loom interface injection.
 */
public interface IZetaSelectionListExtensions {
	default int getLeft() {
		throw new AssertionError("implemented by AbstractSelectionListExtMixin");
	}

	default int getRight() {
		throw new AssertionError("implemented by AbstractSelectionListExtMixin");
	}

	default int getTop() {
		throw new AssertionError("implemented by AbstractSelectionListExtMixin");
	}

	default int getBottom() {
		throw new AssertionError("implemented by AbstractSelectionListExtMixin");
	}

	default int getWidth() {
		throw new AssertionError("implemented by AbstractSelectionListExtMixin");
	}

	default int getHeight() {
		throw new AssertionError("implemented by AbstractSelectionListExtMixin");
	}
}
