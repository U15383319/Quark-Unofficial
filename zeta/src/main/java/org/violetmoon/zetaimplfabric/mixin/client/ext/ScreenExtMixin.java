package org.violetmoon.zetaimplfabric.mixin.client.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.violetmoon.zetaimplfabric.ext.client.IZetaScreenExtensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public abstract class ScreenExtMixin implements IZetaScreenExtensions {

	@Shadow protected Minecraft minecraft;

	@Override
	public Minecraft getMinecraft() {
		return minecraft;
	}
}
