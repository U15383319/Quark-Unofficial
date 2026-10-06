package org.violetmoon.zetaimplfabric.mixin.hook;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zeta.block.SimpleFluidloggedBlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge BucketPickup#getPickupSound(BlockState): fluidlogged Zeta blocks play the sound of the fluid they held.
 */
@Mixin(BucketItem.class)
public class BucketItemHookMixin {

	@WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BucketPickup;getPickupSound()Ljava/util/Optional;"))
	private Optional<SoundEvent> zeta$pickupSound(BucketPickup pickup, Operation<Optional<SoundEvent>> original, @Local(ordinal = 0) BlockState state) {
		return pickup instanceof SimpleFluidloggedBlock fluidlogged ? fluidlogged.getPickupSound(state) : original.call(pickup);
	}
}
