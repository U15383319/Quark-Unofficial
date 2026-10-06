package org.violetmoon.zetaimplfabric.mixin.client.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.violetmoon.zetaimplfabric.ext.client.IZetaWidgetExtensions;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Mixin(AbstractButton.class)
public abstract class AbstractButtonExtMixin extends AbstractWidget {

	public AbstractButtonExtMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	// Forge: the label color comes from getFGColor
	@ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractButton;renderString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"), index = 2)
	private int zeta$fgColor(int color) {
		return ((IZetaWidgetExtensions) this).getFGColor() | Mth.ceil(alpha * 255.0F) << 24;
	}
}
