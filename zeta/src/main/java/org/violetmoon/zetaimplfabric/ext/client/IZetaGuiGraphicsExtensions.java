package org.violetmoon.zetaimplfabric.ext.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Forge's GuiGraphics additions, injected with Loom interface injection.
 */
public interface IZetaGuiGraphicsExtensions {
	/**
	 * Forge: drawString with float coordinates.
	 */
	default int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
		throw new AssertionError("implemented by GuiGraphicsExtMixin");
	}

	/**
	 * Forge: renderComponentTooltip with an item stack (for the item's tooltip font).
	 */
	default void renderComponentTooltip(Font font, List<Component> tooltips, int mouseX, int mouseY, ItemStack stack) {
		throw new AssertionError("implemented by GuiGraphicsExtMixin");
	}
}
