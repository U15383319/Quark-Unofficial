package org.violetmoon.quark.mixin.mixins.client;

import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.violetmoon.quark.content.tools.module.ColorRunesModule;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

	// Fabric: Forge's getArmorModelHook is called from renderArmorPiece with the slot's stack
	@Inject(method = "renderArmorPiece", at = @At("HEAD"))
	private void quark$setColorRuneTargetStack(PoseStack pose, MultiBufferSource buffer, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci) {
		ColorRunesModule.setTargetStack(entity.getItemBySlot(slot));
	}

	// Fabric: vanilla has a single renderGlint (Forge and OptiFine had different signatures, hence the upstream group)
	@ModifyExpressionValue(method = "renderGlint", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorEntityGlint()Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType quark$getArmorGlint(RenderType prev) {
		return ColorRunesModule.Client.getArmorEntityGlint();
	}
}
