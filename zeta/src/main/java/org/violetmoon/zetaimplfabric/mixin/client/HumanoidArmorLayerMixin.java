package org.violetmoon.zetaimplfabric.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zeta.client.HumanoidArmorModelGetter;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.client.FabricZetaClient;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Forge IClientItemExtensions#getHumanoidArmorModel and IForgeItem#getArmorTexture.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, A extends HumanoidModel<T>> {

	@Unique private @Nullable ItemStack zeta$stack;
	@Unique private @Nullable LivingEntity zeta$entity;
	@Unique private @Nullable EquipmentSlot zeta$slot;

	@Inject(method = "renderArmorPiece", at = @At("HEAD"))
	private void zeta$captureContext(PoseStack pose, MultiBufferSource buffer, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci) {
		zeta$stack = entity.getItemBySlot(slot);
		zeta$entity = entity;
		zeta$slot = slot;
	}

	@Inject(method = "renderArmorPiece", at = @At("RETURN"))
	private void zeta$clearContext(PoseStack pose, MultiBufferSource buffer, T entity, EquipmentSlot slot, int light, A model, CallbackInfo ci) {
		zeta$stack = null;
		zeta$entity = null;
		zeta$slot = null;
	}

	@SuppressWarnings("unchecked")
	@ModifyVariable(method = "renderArmorPiece", argsOnly = true,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;setPartVisibility(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V", shift = At.Shift.AFTER))
	private A zeta$armorModel(A model, PoseStack pose, MultiBufferSource buffer, T entity, EquipmentSlot slot) {
		ItemStack stack = entity.getItemBySlot(slot);
		HumanoidArmorModelGetter getter = FabricZetaClient.ARMOR_MODELS.get(stack.getItem());
		if(getter == null)
			return model;
		HumanoidModel<?> replacement = getter.getHumanoidArmorModel(entity, stack, slot, model);
		if(replacement != model)
			copyModelProperties(model, (HumanoidModel<T>) replacement);
		return (A) replacement;
	}

	// Forge's IClientItemExtensions#getGenericArmorModel does this with every replacement model (ForgeHooksClient#copyModelProperties):
	// without it the model keeps the pose it was baked with, so a backpack hangs at the legs and a hat floats away
	@Unique
	private static <T extends LivingEntity> void copyModelProperties(HumanoidModel<T> original, HumanoidModel<T> replacement) {
		original.copyPropertiesTo(replacement);
		replacement.head.visible = original.head.visible;
		replacement.hat.visible = original.hat.visible;
		replacement.body.visible = original.body.visible;
		replacement.rightArm.visible = original.rightArm.visible;
		replacement.leftArm.visible = original.leftArm.visible;
		replacement.rightLeg.visible = original.rightLeg.visible;
		replacement.leftLeg.visible = original.leftLeg.visible;
	}

	@Inject(method = "getArmorLocation", at = @At("HEAD"), cancellable = true)
	private void zeta$armorTexture(ArmorItem armorItem, boolean layer2, @Nullable String suffix, CallbackInfoReturnable<ResourceLocation> cir) {
		if(zeta$stack != null && zeta$stack.getItem() instanceof IZetaItemExtensions ext) {
			String texture = ext.getArmorTextureZeta(zeta$stack, zeta$entity, zeta$slot, suffix);
			if(texture != null)
				cir.setReturnValue(new ResourceLocation(texture));
		}
	}
}
