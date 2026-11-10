package org.violetmoon.zetaimplfabric.mixin.ext;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Resolves Properties#lootFrom (IZetaBlockPropertiesExtensions) the first time the loot table is asked for.
 */
@Mixin(BlockBehaviour.class)
public class BlockBehaviourExtMixin {

	@Shadow protected ResourceLocation drops;
	@Shadow @Final protected BlockBehaviour.Properties properties;

	@Inject(method = "getLootTable", at = @At("HEAD"))
	private void zeta$lootFrom(CallbackInfoReturnable<ResourceLocation> cir) {
		if(drops == null) {
			Supplier<? extends Block> lootFrom = properties.zeta$getLootFrom();
			if(lootFrom != null)
				drops = lootFrom.get().getLootTable();
		}
	}
}
