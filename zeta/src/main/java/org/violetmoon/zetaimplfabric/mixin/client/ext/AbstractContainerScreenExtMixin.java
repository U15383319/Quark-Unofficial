package org.violetmoon.zetaimplfabric.mixin.client.ext;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.violetmoon.zetaimplfabric.ext.client.IZetaContainerScreenExtensions;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenExtMixin implements IZetaContainerScreenExtensions {

	@Shadow protected int leftPos;
	@Shadow protected int topPos;
	@Shadow protected int imageWidth;
	@Shadow protected int imageHeight;
	@Shadow protected @Nullable Slot hoveredSlot;

	@Override
	public int getGuiLeft() {
		return leftPos;
	}

	@Override
	public int getGuiTop() {
		return topPos;
	}

	@Override
	public int getXSize() {
		return imageWidth;
	}

	@Override
	public int getYSize() {
		return imageHeight;
	}

	@Override
	public @Nullable Slot getSlotUnderMouse() {
		return hoveredSlot;
	}
}
