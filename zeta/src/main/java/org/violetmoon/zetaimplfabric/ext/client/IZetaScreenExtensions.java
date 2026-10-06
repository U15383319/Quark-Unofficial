package org.violetmoon.zetaimplfabric.ext.client;

import net.minecraft.client.Minecraft;

/**
 * Forge's Screen#getMinecraft, injected with Loom interface injection.
 */
public interface IZetaScreenExtensions {
	default Minecraft getMinecraft() {
		throw new AssertionError("implemented by ScreenExtMixin");
	}
}
