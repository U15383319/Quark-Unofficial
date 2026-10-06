package org.violetmoon.zetaimplfabric.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

	// Forge ModelEvent.ModifyBakingResult
	@Inject(method = "loadModels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakeModels(Ljava/util/function/BiFunction;)V", shift = At.Shift.AFTER))
	private void zeta$modifyBakingResult(ProfilerFiller profiler, Map<ResourceLocation, AtlasSet.StitchResult> atlases, ModelBakery bakery, CallbackInfoReturnable<?> cir) {
		FabricZClientEvents.fireModifyBakingResult(bakery.getBakedTopLevelModels(), bakery);
	}
}
