package org.violetmoon.zetaimplfabric.mixin.ext;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.ext.IZetaBlockEntityExtensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public abstract class BlockEntityExtMixin implements IZetaBlockEntityExtensions {

	@Unique
	private @Nullable CompoundTag zeta$persistentData;

	@Override
	public CompoundTag getPersistentData() {
		if(zeta$persistentData == null)
			zeta$persistentData = new CompoundTag();
		return zeta$persistentData;
	}

	@Inject(method = "saveWithoutMetadata", at = @At("RETURN"))
	private void zeta$savePersistentData(CallbackInfoReturnable<CompoundTag> cir) {
		if(zeta$persistentData != null)
			cir.getReturnValue().put("ForgeData", zeta$persistentData.copy());
	}

	@Inject(method = "load", at = @At("HEAD"))
	private void zeta$loadPersistentData(CompoundTag tag, CallbackInfo ci) {
		if(tag.contains("ForgeData", Tag.TAG_COMPOUND))
			zeta$persistentData = tag.getCompound("ForgeData");
	}
}
