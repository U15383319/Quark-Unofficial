package org.violetmoon.zetaimplfabric.mixin.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.zetaimplfabric.ext.IZetaItemStackExtensions;

import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackExtMixin implements IZetaItemStackExtensions { }
