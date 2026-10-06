package org.violetmoon.zetaimplfabric.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.util.ZetaSide;

import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Port of zetaimplforge's ConfigEventDispatcher. Same state machine; see that class (commit 3f060db) for the long story.
 * <ul>
 *     <li>Forge FMLCommonSetupEvent#enqueueWork -> {@link #commonSetup()}, called by FabricZeta when it fires ZCommonSetup</li>
 *     <li>Forge ServerAboutToStartEvent -> ServerLifecycleEvents.SERVER_STARTING</li>
 *     <li>Forge ModConfigEvent.Reloading -> Forge Config API Port's ModConfigEvents.reloading</li>
 * </ul>
 */
public class ConfigEventDispatcher {

	private static final int BEFORE_INIT = 0;
	private static final int WAITING_FOR_SERVER_START = 1;
	private static final int ACCEPT_FILE_RELOADS = 2;
	private static final int BUSY = 3;

	private final Zeta z;
	private final AtomicInteger state = new AtomicInteger(BEFORE_INIT);

	public ConfigEventDispatcher(Zeta z) {
		this.z = z;

		ModConfigEvents.reloading(z.modid).register(this::modConfigReloading);
		ServerLifecycleEvents.SERVER_STARTING.register(server -> serverAboutToStart());
	}

	public void commonSetup() {
		z.log.info("Common setup: Performing initial refresh of {}'s config on thread '{}'", z.modid, Thread.currentThread().getName());
		z.configManager.onReload();
		z.loadBus.fire(new ZConfigChanged() { }, ZConfigChanged.class);

		int oldState;
		if(z.side == ZetaSide.CLIENT) {
			//on the client we always have a render thread, we can start accepting config refreshes now
			oldState = state.getAndSet(ACCEPT_FILE_RELOADS);
			z.log.info("{}'s config is now ready to accept filewatcher changes", z.modid);
		} else {
			//on the server we want to wait for the existence of the server thread to process config refreshes on
			oldState = state.getAndSet(WAITING_FOR_SERVER_START);
			z.log.info("Waiting for server start before accepting filewatcher changes to {}'s config", z.modid);
		}

		if(oldState != BEFORE_INIT)
			z.log.warn("Common setup: {}'s config was previously in state '{}'... weird, but trying to continue. Report this.", z.modid, fmtState(oldState));
	}

	private void serverAboutToStart() {
		if(z.side == ZetaSide.SERVER) {
			int oldState = state.compareAndExchange(WAITING_FOR_SERVER_START, ACCEPT_FILE_RELOADS);
			if(oldState == WAITING_FOR_SERVER_START)
				z.log.info("Server starting, accepting filewatcher changes for {}'s config", z.modid);
		}
	}

	private void modConfigReloading(ModConfig config) {
		if(z.configManager == null || z.configInternals == null) {
			z.log.info("Ignoring request to refresh {}'s config WAY too early", z.modid);
			return;
		}

		z.log.info("About to refresh {}'s config, looking for better thread than '{}'...", z.modid, Thread.currentThread().getName());
		z.proxy.tryToExecuteOnMainThread(() -> {
			int oldState = state.compareAndExchange(ACCEPT_FILE_RELOADS, BUSY);
			if(oldState != ACCEPT_FILE_RELOADS) {
				z.log.info("{}'s config is '{}', ignoring config refresh. Current thread: {}", z.modid, fmtState(oldState), Thread.currentThread().getName());
				return;
			} else
				z.log.info("Refreshing {}'s config on thread '{}'", z.modid, Thread.currentThread().getName());

			try {
				z.configManager.onReload();
				z.loadBus.fire(new ZConfigChanged() { }, ZConfigChanged.class);
			} finally {
				z.log.info("All done refreshing {}'s config", z.modid);
				state.setRelease(ACCEPT_FILE_RELOADS);
			}
		});
	}

	private static String fmtState(int state) {
		return switch(state) {
			case BEFORE_INIT -> "BEFORE_INIT";
			case WAITING_FOR_SERVER_START -> "WAITING_FOR_SERVER_START";
			case ACCEPT_FILE_RELOADS -> "ACCEPT_FILE_RELOADS";
			case BUSY -> "BUSY";
			default -> "weird unknown state " + state + "???";
		};
	}
}
