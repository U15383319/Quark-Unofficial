package org.violetmoon.zetaimplfabric.mod;

import org.violetmoon.zeta.client.ClientTicker;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.ZItemTooltip;
import org.violetmoon.zeta.item.IZetaItem;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.mod.ZetaMod;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zetaimplfabric.client.ZetaSpawnDataClient;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/**
 * Zeta's client entrypoint (Forge: ZetaModClientProxy).
 */
public class ZetaModFabricClient implements ClientModInitializer {

	private static boolean initialized = false;

	@Override
	public void onInitializeClient() {
		ensureInitialized();
	}

	public static synchronized void ensureInitialized() {
		if(initialized)
			return;
		initialized = true;

		ZetaModFabric.ensureInitialized();
		FabricZClientEvents.init();
		ZetaSpawnDataClient.init();

		ZetaMod.ZETA.playBus
			.subscribe(ClientTicker.INSTANCE)
			.subscribe(ZetaModFabricClient.class);
	}

	@PlayEvent
	public static void onTooltip(ZItemTooltip event) {
		Item item = event.getItemStack().getItem();

		ZetaModule module = null;
		if(item instanceof IZetaItem zi)
			module = zi.getModule();
		if(item instanceof BlockItem bi && bi.getBlock() instanceof IZetaBlock zb)
			zb.getModule(); // (sic) upstream Forge code ignores this result too

		if(module != null && !module.isEnabled() && module.category().isAddon())
			event.getToolTip().add(module.category().getDisabledTooltip());
	}
}
