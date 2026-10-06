package org.violetmoon.zetaimplfabric.mixin.client;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	@Shadow private Map<EntityType<?>, EntityRenderer<?>> renderers;
	@Shadow @Final private EntityModelSet entityModels;

	// Forge EntityRenderersEvent.AddLayers
	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void zeta$addLayers(ResourceManager resourceManager, CallbackInfo ci) {
		FabricZClientEvents.fireAddModelLayers(new HashMap<>(renderers), entityModels);
	}
}
