package org.violetmoon.zetaimplfabric.mod;

import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.config.SyncedFlagHandler;
import org.violetmoon.zeta.config.ZetaGeneralConfig;
import org.violetmoon.zeta.mod.ZetaMod;
import org.violetmoon.zeta.util.handler.RecipeCrawlHandler;
import org.violetmoon.zeta.util.handler.ToolInteractionHandler;
import org.violetmoon.zeta.util.zetalist.ZetaList;
import org.violetmoon.zeta.world.EntitySpawnHandler;
import org.violetmoon.zeta.world.WorldGenHandler;
import org.violetmoon.zetaimplfabric.FabricZeta;
import org.violetmoon.zetaimplfabric.entity.IZetaEntityExtensions;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;
import org.violetmoon.zetaimplfabric.event.play.FabricZPlayerEvents;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;
import org.violetmoon.zetaimplfabric.util.ServerTracker;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Zeta's own mod entrypoint (Forge: ZetaModForge + ZetaModCommonProxy).
 * <p>
 * Mods built on Zeta call {@link #ensureInitialized()} first in their own ModInitializer, because Fabric does not
 * guarantee that Zeta's entrypoint runs before theirs.
 */
public class ZetaModFabric implements ModInitializer {

	private static boolean initialized = false;
	private static boolean loadCompleteFired = false;

	@Override
	public void onInitialize() {
		ensureInitialized();
	}

	public static synchronized void ensureInitialized() {
		if(initialized)
			return;
		initialized = true;

		ServerTracker.init();
		FabricZPlayerEvents.init();
		FabricZEntityEvents.init();
		FabricZWorldEvents.init();

		// Forge copies the PlayerPersisted sub-tag of the persistent data when a player respawns or leaves the end
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			String key = IZetaEntityExtensions.PERSISTED_NBT_TAG;
			if(oldPlayer.getPersistentData().contains(key))
				newPlayer.getPersistentData().put(key, oldPlayer.getPersistentData().getCompound(key).copy());
		});

		// ZLoadComplete: the client fires it from MinecraftMixin, after every mod has initialized
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
			ServerLifecycleEvents.SERVER_STARTING.register(server -> fireLoadComplete());

		FabricZeta zeta = new FabricZeta(ZetaMod.ZETA_ID, ZetaMod.LOGGER);
		new ZetaMod(zeta);
		registerEvents(zeta);
		zeta.finishCommonSetup();
	}

	private static void registerEvents(Zeta zeta) {
		zeta.loadBus
			.subscribe(RecipeCrawlHandler.class)
			.subscribe(ToolInteractionHandler.class)
			.subscribe(EntitySpawnHandler.class)
			.subscribe(WorldGenHandler.class)
			.subscribe(ZetaGeneralConfig.class);

		zeta.playBus
			.subscribe(RecipeCrawlHandler.class)
			.subscribe(ToolInteractionHandler.class)
			.subscribe(SyncedFlagHandler.class);
	}

	/**
	 * Forge FMLLoadCompleteEvent: fired once, after every mod finished initializing.
	 */
	public static synchronized void fireLoadComplete() {
		if(loadCompleteFired)
			return;
		loadCompleteFired = true;

		for(Zeta z : ZetaList.INSTANCE.getZetas())
			if(z instanceof FabricZeta fz)
				fz.fireLoadComplete();
	}
}
