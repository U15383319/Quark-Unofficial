package org.violetmoon.zetaimplfabric.mixin.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;
import org.violetmoon.zetaimplfabric.ext.client.IZetaGuiGraphicsExtensions;

import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements IZetaGuiGraphicsExtensions {

	@Shadow
	public abstract int guiWidth();

	@Shadow
	public abstract int guiHeight();

	@Shadow
	protected abstract void renderTooltipInternal(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner);

	/**
	 * Forge RenderTooltipEvent.GatherComponents for item tooltips (ForgeHooksClient#gatherTooltipComponents),
	 * including Forge's line wrapping.
	 */
	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
	private void zeta$gatherTooltip(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
		renderTooltip(font, Screen.getTooltipFromItem(Minecraft.getInstance(), stack), stack.getTooltipImage(), stack, mouseX, mouseY);
		ci.cancel();
	}

	@Override
	public void renderTooltip(Font font, List<Component> lines, Optional<TooltipComponent> image, ItemStack stack, int mouseX, int mouseY) {
		List<Either<FormattedText, TooltipComponent>> elements = new ArrayList<>();
		for(Component line : lines)
			elements.add(Either.left(line));
		image.ifPresent(i -> elements.add(Math.min(1, elements.size()), Either.right(i)));

		int maxWidth = FabricZClientEvents.fireGatherTooltip(stack, guiWidth(), guiHeight(), elements, -1);

		int textWidth = elements.stream().mapToInt(e -> e.map(font::width, c -> 0)).max().orElse(0);
		boolean wrap = false;
		int screenWidth = guiWidth();
		int tooltipX = mouseX + 12;
		if(tooltipX + textWidth + 4 > screenWidth) {
			tooltipX = mouseX - 16 - textWidth;
			if(tooltipX < 4) {
				textWidth = mouseX > screenWidth / 2 ? mouseX - 12 - 8 : screenWidth - 16 - mouseX;
				wrap = true;
			}
		}
		if(maxWidth > 0 && textWidth > maxWidth) {
			textWidth = maxWidth;
			wrap = true;
		}

		int width = textWidth;
		boolean doWrap = wrap;
		List<ClientTooltipComponent> components = elements.stream()
			.flatMap(e -> e.map(
				text -> doWrap ? zeta$split(font, text, width) : Stream.of(ClientTooltipComponent.create(zeta$visual(text))),
				component -> Stream.of(ClientTooltipComponent.create(component))))
			.toList();

		renderTooltipInternal(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
	}

	private static FormattedCharSequence zeta$visual(FormattedText text) {
		return text instanceof Component c ? c.getVisualOrderText() : Language.getInstance().getVisualOrder(text);
	}

	private static Stream<ClientTooltipComponent> zeta$split(Font font, FormattedText text, int width) {
		if(text instanceof Component c && c.getString().isEmpty())
			return Stream.of(ClientTooltipComponent.create(c.getVisualOrderText()));
		return font.split(text, width).stream().map(ClientTooltipComponent::create);
	}
}
