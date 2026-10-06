package org.violetmoon.zetaimplfabric.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;
import org.violetmoon.zetaimplfabric.mod.ZetaModFabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

	@Shadow public @Nullable Screen screen;

	@Shadow
	public abstract void setScreen(@Nullable Screen guiScreen);

	@Unique
	private boolean zeta$replacingScreen = false;

	// Forge FMLLoadCompleteEvent: every mod's entrypoints have run by the time the color registries are created
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/block/BlockColors;createDefault()Lnet/minecraft/client/color/block/BlockColors;"))
	private void zeta$loadComplete(CallbackInfo ci) {
		ZetaModFabric.fireLoadComplete();
	}

	// Forge ScreenEvent.Opening
	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void zeta$screenOpening(@Nullable Screen guiScreen, CallbackInfo ci) {
		if(guiScreen == null || zeta$replacingScreen)
			return;

		FabricZClientEvents.ScreenOpening event = FabricZClientEvents.fireScreenOpening(screen, guiScreen);
		if(event.isCanceled()) {
			ci.cancel();
		} else if(event.getNewScreen() != guiScreen) {
			ci.cancel();
			zeta$replacingScreen = true;
			try {
				setScreen(event.getNewScreen());
			} finally {
				zeta$replacingScreen = false;
			}
		}
	}

	// Forge TickEvent.RenderTickEvent
	@WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
	private void zeta$renderTick(GameRenderer renderer, float partialTicks, long nanoTime, boolean renderLevel, Operation<Void> original) {
		FabricZClientEvents.fireRenderTick(partialTicks, true);
		original.call(renderer, partialTicks, nanoTime, renderLevel);
		FabricZClientEvents.fireRenderTick(partialTicks, false);
	}
}
