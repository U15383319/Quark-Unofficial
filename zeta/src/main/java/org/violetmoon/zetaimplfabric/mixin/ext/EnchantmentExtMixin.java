package org.violetmoon.zetaimplfabric.mixin.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.zetaimplfabric.ext.IZetaEnchantmentExtensions;

import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(Enchantment.class)
public abstract class EnchantmentExtMixin implements IZetaEnchantmentExtensions { }
