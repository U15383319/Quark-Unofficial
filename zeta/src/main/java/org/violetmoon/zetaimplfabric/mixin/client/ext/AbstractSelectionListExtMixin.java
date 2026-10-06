package org.violetmoon.zetaimplfabric.mixin.client.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.violetmoon.zetaimplfabric.ext.client.IZetaSelectionListExtensions;

import net.minecraft.client.gui.components.AbstractSelectionList;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListExtMixin implements IZetaSelectionListExtensions {

	@Shadow protected int x0;
	@Shadow protected int x1;
	@Shadow protected int y0;
	@Shadow protected int y1;
	@Shadow protected int width;
	@Shadow protected int height;

	@Override
	public int getLeft() {
		return x0;
	}

	@Override
	public int getRight() {
		return x1;
	}

	@Override
	public int getTop() {
		return y0;
	}

	@Override
	public int getBottom() {
		return y1;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
