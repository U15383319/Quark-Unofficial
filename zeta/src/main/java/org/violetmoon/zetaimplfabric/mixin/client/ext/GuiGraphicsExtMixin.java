package org.violetmoon.zetaimplfabric.mixin.client.ext;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.violetmoon.zetaimplfabric.ext.client.IZetaGuiGraphicsExtensions;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsExtMixin implements IZetaGuiGraphicsExtensions {

	@Shadow @Final private PoseStack pose;
	@Shadow @Final private MultiBufferSource.BufferSource bufferSource;

	@Shadow
	protected abstract void flushIfUnmanaged();

	@Shadow
	public abstract void renderComponentTooltip(Font font, List<Component> tooltips, int mouseX, int mouseY);

	@Override
	public int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
		if(text == null)
			return 0;
		int width = font.drawInBatch(text, x, y, color, dropShadow, pose.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, font.isBidirectional());
		flushIfUnmanaged();
		return width;
	}

	@Override
	public void renderComponentTooltip(Font font, List<Component> tooltips, int mouseX, int mouseY, ItemStack stack) {
		renderComponentTooltip(font, tooltips, mouseX, mouseY);
	}
}
