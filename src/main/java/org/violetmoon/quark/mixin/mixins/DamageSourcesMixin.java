package org.violetmoon.quark.mixin.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.tools.module.PickarangModule;

@Mixin(DamageSources.class)
public class DamageSourcesMixin {

    @Inject(method = "playerAttack", at = @At("HEAD"), cancellable = true)
    private void playerAttack(Player player, CallbackInfoReturnable<DamageSource> callbackInfoReturnable) {
        //Needed since pickarang needs to both have unique damage source AND go through the player attack logic to pickup enchantments and whatnot
        DamageSource damage = PickarangModule.getActiveDamage();
        if (damage != null)
            callbackInfoReturnable.setReturnValue(damage);
    }
}
