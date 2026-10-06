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

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	@Shadow @Final private Minecraft minecraft;

	// Forge InputEvent.MouseButton.Post
	@Inject(method = "onPress", at = @At("TAIL"))
	private void zeta$mouseInput(long window, int button, int action, int modifiers, CallbackInfo ci) {
		if(window == minecraft.getWindow().getWindow())
			FabricZClientEvents.fireMouseInput(button, action);
	}

	// Forge ScreenEvent.MouseButtonPressed.Pre/Post
	// (the call lives in onPress's static lambda, intermediary name)
	@WrapOperation(method = "method_1611", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"))
	private static boolean zeta$screenMouseClicked(Screen screen, double x, double y, int button, Operation<Boolean> original) {
		if(FabricZClientEvents.fireMouseClickPre(screen, x, y, button))
			return true;
		boolean handled = original.call(screen, x, y, button);
		if(!handled)
			handled = FabricZClientEvents.fireMouseClickPost(screen, x, y, button);
		return handled;
	}

	// Forge ScreenEvent.MouseScrolled.Pre/Post
	@WrapOperation(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDD)Z"))
	private boolean zeta$screenMouseScrolled(Screen screen, double x, double y, double delta, Operation<Boolean> original) {
		if(FabricZClientEvents.fireMouseScrollPre(screen, delta))
			return true;
		boolean handled = original.call(screen, x, y, delta);
		if(!handled)
			handled = FabricZClientEvents.fireMouseScrollPost(screen, delta);
		return handled;
	}
}
