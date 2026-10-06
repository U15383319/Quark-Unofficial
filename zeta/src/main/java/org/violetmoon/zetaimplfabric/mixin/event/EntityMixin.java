package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.entity.IZetaEntityExtensions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Forge IForgeEntity bits: persistent data (saved as "ForgeData") and drop capturing.
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements IZetaEntityExtensions {

	@Unique
	private @Nullable CompoundTag zeta$persistentData;

	@Unique
	private @Nullable Collection<ItemEntity> zeta$capturedDrops;

	@Override
	public CompoundTag getPersistentData() {
		if(zeta$persistentData == null)
			zeta$persistentData = new CompoundTag();
		return zeta$persistentData;
	}

	@Override
	public @Nullable Collection<ItemEntity> captureDrops() {
		return zeta$capturedDrops;
	}

	@Override
	public @Nullable Collection<ItemEntity> captureDrops(@Nullable Collection<ItemEntity> value) {
		Collection<ItemEntity> old = zeta$capturedDrops;
		zeta$capturedDrops = value;
		return old;
	}

	@Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void zeta$savePersistentData(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		if(zeta$persistentData != null)
			compound.put("ForgeData", zeta$persistentData.copy());
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	private void zeta$loadPersistentData(CompoundTag compound, CallbackInfo ci) {
		if(compound.contains("ForgeData", Tag.TAG_COMPOUND))
			zeta$persistentData = compound.getCompound("ForgeData");
	}

	@WrapOperation(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean zeta$captureDrop(Level level, Entity entity, Operation<Boolean> original, ItemStack stack, float offsetY) {
		if(zeta$capturedDrops != null && entity instanceof ItemEntity item) {
			zeta$capturedDrops.add(item);
			return true;
		}
		return original.call(level, entity);
	}
}
