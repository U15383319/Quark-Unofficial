package org.violetmoon.zetaimplfabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	// Zeta's own ZEarlyRender (same injection point as zetaimplforge's GameRenderMixin)
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = At.Shift.AFTER))
	private void zeta$earlyRender(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci, @Local GuiGraphics guiGraphics) {
		FabricZClientEvents.fireEarlyRender(guiGraphics);
	}
}
