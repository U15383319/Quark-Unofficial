package org.violetmoon.zetaimplfabric.mixin.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.zetaimplfabric.ext.IZetaBlockExtras;

import net.minecraft.world.level.block.Block;

@Mixin(Block.class)
public abstract class BlockExtMixin implements IZetaBlockExtras { }
