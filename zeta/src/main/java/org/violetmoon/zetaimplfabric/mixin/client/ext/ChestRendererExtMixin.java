package org.violetmoon.zetaimplfabric.mixin.client.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.ext.client.IZetaChestRendererExtensions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

@Mixin(ChestRenderer.class)
public abstract class ChestRendererExtMixin<T extends BlockEntity> implements IZetaChestRendererExtensions<T> {

	@SuppressWarnings("unchecked")
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Sheets;chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;"))
	private Material zeta$getMaterial(BlockEntity blockEntity, ChestType chestType, boolean holiday, Operation<Material> original) {
		Material material = getMaterial((T) blockEntity, chestType);
		return material != null ? material : original.call(blockEntity, chestType, holiday);
	}
}
