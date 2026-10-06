package org.violetmoon.quark.mixin.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.client.module.ElytraIndicatorModule;

import net.minecraft.client.gui.Gui;

/**
 * Fabric replacement for ForgeGuiMixin: Forge's armor overlay loops up to 20 half-icons, vanilla's armor row loops over
 * 10 icons; the elytra indicator takes one of them.
 */
@Mixin(Gui.class)
public class GuiArmorLimitMixin {

	@ModifyConstant(method = "renderPlayerHealth", constant = @Constant(intValue = 10),
		slice = @Slice(
			from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = "ldc=armor"),
			to = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=health")))
	private int quark$armorLimit(int original) {
		ElytraIndicatorModule module = Quark.ZETA.modules.get(ElytraIndicatorModule.class);
		return module == null ? original : module.getArmorLimit(original * 2) / 2;
	}
}
