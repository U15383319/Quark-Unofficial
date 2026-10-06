package org.violetmoon.quark.mixin.mixins.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import org.violetmoon.quark.content.management.module.EasyTransferingModule;

@Mixin(AbstractContainerScreen.class)
public class ContainerScreenMixin {

	@ModifyVariable(
		method = "mouseClicked(DDI)Z",
		at = @At("STORE"),
		index = 14 // Fabric: vanilla local index (15 with Forge patches)
	)
	private boolean hasShiftDownClick(boolean curr) {
		return EasyTransferingModule.Client.hasShiftDown(curr);
	}

	@ModifyVariable(
		method = "mouseReleased(DDI)Z",
		at = @At("STORE"),
		index = 11 // Fabric: vanilla local index (12 with Forge patches)
	)
	private boolean hasShiftDownRelease(boolean curr) {
		return EasyTransferingModule.Client.hasShiftDown(curr);
	}

}
