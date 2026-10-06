package org.violetmoon.zetaimplfabric.mixin.client;

import java.io.File;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {

	// Forge ScreenshotEvent
	@Inject(method = "_grab", at = @At("HEAD"))
	private static void zeta$screenshot(File gameDirectory, @Nullable String name, RenderTarget buffer, Consumer<Component> messageConsumer, CallbackInfo ci) {
		FabricZClientEvents.fireScreenshot();
	}
}
