package org.violetmoon.zetaimplfabric.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.client.ClientRegistryExtension;
import org.violetmoon.zeta.client.HumanoidArmorModelGetter;
import org.violetmoon.zeta.client.ZetaClient;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zetaimplfabric.util.ServerTracker;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * Fabric replacement for ForgeZetaClient.
 */
public class FabricZetaClient extends ZetaClient {

	/**
	 * Forge's IClientItemExtensions#getHumanoidArmorModel; read by HumanoidArmorLayerMixin.
	 */
	public static final Map<Item, HumanoidArmorModelGetter> ARMOR_MODELS = new ConcurrentHashMap<>();

	public FabricZetaClient(Zeta z) {
		super(z);
	}

	@Override
	public @Nullable BlockColor getBlockColor(BlockColors bcs, Block block) {
		return ColorProviderRegistry.BLOCK.get(block);
	}

	@Override
	public @Nullable ItemColor getItemColor(ItemColors ics, ItemLike itemlike) {
		return ColorProviderRegistry.ITEM.get(itemlike.asItem());
	}

	@Override
	public ClientRegistryExtension createClientRegistryExtension() {
		return new ClientRegistryExtension(zeta) {
			@Override
			protected void doSetRenderLayer(Block block, RenderLayerRegistry.Layer layer) {
				BlockRenderLayerMap.INSTANCE.putBlock(block, resolvedTypes.get(layer));
			}
		};
	}

	@Override
	public void setBlockEntityWithoutLevelRenderer(Item item, BlockEntityWithoutLevelRenderer bewlr) {
		// Forge's IClientItemExtensions#getCustomRenderer; the item model must still be builtin/entity
		BuiltinItemRendererRegistry.INSTANCE.register(item, bewlr::renderByItem);
	}

	@Override
	public void setHumanoidArmorModel(Item item, HumanoidArmorModelGetter modelGetter) {
		ARMOR_MODELS.put(item, modelGetter);
	}

	@Override
	public @Nullable RegistryAccess hackilyGetCurrentClientLevelRegistryAccess() {
		Minecraft mc = Minecraft.getInstance();
		if(!mc.isSameThread()) {
			MinecraftServer server = ServerTracker.getCurrentServer();
			if(server != null)
				return server.registryAccess();
		}

		ClientPacketListener conn = mc.getConnection();
		return conn == null ? null : conn.registryAccess();
	}

	/**
	 * Equivalent of the Forge client mod-bus events (FMLClientSetupEvent and friends). Call at the end of the mod's
	 * ClientModInitializer.
	 */
	public void finishClientSetup() {
		FabricZetaClientSetup.finish(this);
	}
}
