package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

	// Forge AddReloadListenerEvent: extra listeners run after the vanilla (and Fabric) ones
	@ModifyExpressionValue(method = "loadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerResources;listeners()Ljava/util/List;"))
	private static List<PreparableReloadListener> zeta$addReloadListeners(List<PreparableReloadListener> listeners,
																		 ResourceManager resourceManager, RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures,
																		 Commands.CommandSelection commandSelection, int functionCompilationLevel, Executor backgroundExecutor, Executor gameExecutor,
																		 @com.llamalad7.mixinextras.sugar.Local ReloadableServerResources resources) {
		List<PreparableReloadListener> result = new ArrayList<>(listeners);
		result.addAll(FabricZWorldEvents.fireAddReloadListener(resources, registryAccess));
		return result;
	}
}
