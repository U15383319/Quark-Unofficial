package org.violetmoon.zetaimplfabric.ext.client;

import net.minecraft.client.gui.components.AbstractWidget;

/**
 * Forge's AbstractWidget#getFGColor (the button label color), injected with Loom interface injection and used by
 * AbstractButtonMixin.
 */
public interface IZetaWidgetExtensions {
	default int getFGColor() {
		return ((AbstractWidget) this).active ? 0xFFFFFF : 0xA0A0A0;
	}
}
