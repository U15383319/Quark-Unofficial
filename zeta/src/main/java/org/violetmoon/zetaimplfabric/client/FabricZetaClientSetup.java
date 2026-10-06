package org.violetmoon.zetaimplfabric.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zeta.client.event.load.ZAddModels;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.client.event.load.ZModel;
import org.violetmoon.zeta.client.event.load.ZRegisterClientReloadListener;
import org.violetmoon.zeta.client.event.load.ZRegisterLayerDefinitions;
import org.violetmoon.zeta.client.event.load.ZTooltipComponents;
import org.violetmoon.zetaimplfabric.ext.ZetaHooks;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fires the client-side load events that Forge sends on the mod bus during client startup.
 */
final class FabricZetaClientSetup {

	private FabricZetaClientSetup() { }

	static void finish(FabricZetaClient client) {
		Zeta z = client.zeta;

		// Forge RegisterKeyMappingsEvent
		z.loadBus.fire((ZKeyMapping) KeyBindingHelper::registerKeyBinding, ZKeyMapping.class);

		// Forge EntityRenderersEvent.RegisterLayerDefinitions
		z.loadBus.fire(new ZRegisterLayerDefinitions() {
			@Override
			public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
				EntityModelLayerRegistry.registerModelLayer(layerLocation, supplier::get);
			}
		}, ZRegisterLayerDefinitions.class);

		// Forge RegisterClientTooltipComponentFactoriesEvent
		z.loadBus.fire(new ZTooltipComponents() {
			@Override
			public <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory) {
				TooltipComponentCallback.EVENT.register(data -> type.isInstance(data) ? factory.apply(type.cast(data)) : null);
			}
		}, ZTooltipComponents.class);

		// Forge RegisterClientReloadListenersEvent
		List<PreparableReloadListener> reloadListeners = new ArrayList<>();
		z.loadBus.fire((ZRegisterClientReloadListener) reloadListeners::add, ZRegisterClientReloadListener.class);
		for(int i = 0; i < reloadListeners.size(); i++)
			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				new IdentifiedReloadListener(new ResourceLocation(z.modid, "zeta_client_reload_" + i), reloadListeners.get(i)));

		// Forge ModelEvent.RegisterAdditional, fired on every model reload
		ModelLoadingPlugin.register(ctx -> {
			z.loadBus.fire((ZAddModels) ctx::addModels, ZAddModels.class);
			z.loadBus.fire((ZModel.RegisterAdditional) ctx::addModels, ZModel.RegisterAdditional.class);
		});

		// Forge IForgeBlock#shouldDisplayFluidOverlay: Fabric draws the overlay texture next to "transparent" blocks
		for(Block block : BuiltInRegistries.BLOCK) {
			if(!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(z.modid))
				continue;
			BlockState state = block.defaultBlockState();
			IZetaBlockExtensions ext = ZetaHooks.block(state, "shouldDisplayFluidOverlayZeta");
			if(ext != null && ext.shouldDisplayFluidOverlayZeta(state, null, null, state.getFluidState()))
				FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(block, true);
		}

		// Forge FMLClientSetupEvent; enqueued work runs after every listener
		List<Runnable> work = new ArrayList<>();
		z.loadBus.fire((ZClientSetup) work::add, ZClientSetup.class);
		work.forEach(Runnable::run);
	}

	private record IdentifiedReloadListener(ResourceLocation id, PreparableReloadListener inner) implements IdentifiableResourceReloadListener {
		@Override
		public ResourceLocation getFabricId() {
			return id;
		}

		@Override
		public Collection<ResourceLocation> getFabricDependencies() {
			return List.of();
		}

		@Override
		public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepProfiler, ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
			return inner.reload(barrier, manager, prepProfiler, applyProfiler, backgroundExecutor, gameExecutor);
		}

		@Override
		public String getName() {
			return inner.getName();
		}
	}
}
