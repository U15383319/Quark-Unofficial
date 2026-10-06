package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZPlayerEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	// Forge PlayerDestroyItemEvent: a held item broke from durability loss
	@WrapOperation(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
	private <T extends LivingEntity> void zeta$destroyItem(ItemStack stack, int amount, Operation<Void> original, int damage, T entity, Consumer<T> onBroken) {
		if(!(entity instanceof Player player)) {
			original.call(stack, amount);
			return;
		}

		InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : player.getOffhandItem() == stack ? InteractionHand.OFF_HAND : null;
		ItemStack copy = stack.copy();
		original.call(stack, amount);
		if(hand != null)
			FabricZPlayerEvents.fireDestroyItem(player, copy, hand);
	}
}
