package org.violetmoon.zetaimplfabric.mixin.client;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

	// Forge ContainerScreenEvent.Render.Background
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V", shift = At.Shift.AFTER))
	private void zeta$background(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		FabricZClientEvents.fireContainerScreen((AbstractContainerScreen<?>) (Object) this, guiGraphics, mouseX, mouseY, false);
	}

	// Forge ContainerScreenEvent.Render.Foreground (the pose is translated to the gui corner at this point)
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V", shift = At.Shift.AFTER))
	private void zeta$foreground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		FabricZClientEvents.fireContainerScreen((AbstractContainerScreen<?>) (Object) this, guiGraphics, mouseX, mouseY, true);
	}

	// Forge: the hovered item's tooltip goes through the stack-aware renderTooltip (GuiGraphicsMixin), so tooltip
	// components are gathered for it (Quark's food, attribute, shulker box, map... tooltips)
	@WrapOperation(method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"))
	private void zeta$itemTooltip(GuiGraphics guiGraphics, Font font, List<Component> lines, Optional<TooltipComponent> image, int x, int y,
								  Operation<Void> original, @Local(ordinal = 0) ItemStack stack) {
		guiGraphics.renderTooltip(font, lines, image, stack, x, y);
	}
}
