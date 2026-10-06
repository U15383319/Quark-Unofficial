package org.violetmoon.zetaimplfabric.mixin.hook;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * The Forge ItemStack patches that route to the item: getMaxDamage(stack), isDamageable(stack), damageItem and
 * getDefaultTooltipHideFlags.
 */
@Mixin(ItemStack.class)
public class ItemStackHookMixin {

	@Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
	private void zeta$maxDamage(CallbackInfoReturnable<Integer> cir) {
		ItemStack self = (ItemStack) (Object) this;
		IZetaItemExtensions ext = ZetaHooks.item(self, "getMaxDamageZeta");
		if(ext != null)
			cir.setReturnValue(ext.getMaxDamageZeta(self));
	}

	// Forge: isDamageableItem asks Item#isDamageable(stack), which is canBeDepleted() (vanilla: getMaxDamage() > 0)
	@ModifyExpressionValue(method = "isDamageableItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getMaxDamage()I"))
	private int zeta$isDamageable(int maxDamage) {
		ItemStack self = (ItemStack) (Object) this;
		if(ZetaHooks.item(self, "getMaxDamageZeta") == null)
			return maxDamage;
		return self.getItem().canBeDepleted() ? 1 : 0;
	}

	@ModifyVariable(method = "hurtAndBreak", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private <T extends LivingEntity> int zeta$damageItem(int amount, int amountArg, T entity, Consumer<T> onBroken) {
		ItemStack self = (ItemStack) (Object) this;
		IZetaItemExtensions ext = ZetaHooks.item(self, "damageItemZeta");
		return ext != null ? ext.damageItemZeta(self, amount, entity, onBroken) : amount;
	}

	@Inject(method = "getHideFlags", at = @At("HEAD"), cancellable = true)
	private void zeta$defaultHideFlags(CallbackInfoReturnable<Integer> cir) {
		ItemStack self = (ItemStack) (Object) this;
		if(self.hasTag() && self.getTag().contains("HideFlags", 99))
			return;
		IZetaItemExtensions ext = ZetaHooks.item(self, "getDefaultTooltipHideFlagsZeta");
		if(ext != null)
			cir.setReturnValue(ext.getDefaultTooltipHideFlagsZeta(self));
	}
}
