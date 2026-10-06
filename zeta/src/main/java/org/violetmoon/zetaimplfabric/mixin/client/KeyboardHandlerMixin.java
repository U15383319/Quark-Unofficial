package org.violetmoon.zetaimplfabric.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

	@Shadow @Final private Minecraft minecraft;

	// Forge InputEvent.Key, fired at the end of keyPress (not when a screen consumed the key)
	@Inject(method = "keyPress", at = @At("TAIL"))
	private void zeta$keyInput(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
		if(window == minecraft.getWindow().getWindow())
			FabricZClientEvents.fireKeyInput(key, scanCode, action);
	}

	// Forge ScreenEvent.KeyPressed.Pre/Post; the call lives in keyPress's static lambda (intermediary name)
	@WrapOperation(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(III)Z"))
	private static boolean zeta$screenKeyPressed(Screen screen, int key, int scanCode, int modifiers, Operation<Boolean> original) {
		if(FabricZClientEvents.fireKeyPressed(screen, key, scanCode, modifiers, true))
			return true;
		boolean handled = original.call(screen, key, scanCode, modifiers);
		if(!handled)
			handled = FabricZClientEvents.fireKeyPressed(screen, key, scanCode, modifiers, false);
		return handled;
	}

	// Forge ScreenEvent.CharacterTyped.Pre/Post
	// (charTyped's two static lambdas: code points with and without surrogate pairs)
	@WrapOperation(method = { "method_1458", "method_1473" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z"))
	private static boolean zeta$screenCharTyped(GuiEventListener listener, char codePoint, int modifiers, Operation<Boolean> original) {
		if(!(listener instanceof Screen screen))
			return original.call(listener, codePoint, modifiers);
		if(FabricZClientEvents.fireCharTyped(screen, codePoint, modifiers, true))
			return true;
		boolean handled = original.call(listener, codePoint, modifiers);
		if(!handled)
			handled = FabricZClientEvents.fireCharTyped(screen, codePoint, modifiers, false);
		return handled;
	}
}
