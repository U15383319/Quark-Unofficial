package org.violetmoon.zetaimplfabric.event;

import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.event.bus.IZetaLoadEvent;
import org.violetmoon.zeta.event.bus.IZetaPlayEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.util.zetalist.ZetaList;

/**
 * Entry points used by Fabric hooks (callbacks and mixins) to fire Zeta events.
 * <p>
 * On Forge every Zeta's play bus delegated to MinecraftForge.EVENT_BUS. Here all Zetas share one
 * {@link FabricZetaEventBus}, so firing a play event once reaches every mod.
 */
public final class FabricZetaEvents {

	private static volatile FabricZetaEventBus<IZetaPlayEvent> playBus;

	private FabricZetaEvents() { }

	public static synchronized FabricZetaEventBus<IZetaPlayEvent> getOrCreatePlayBus(Zeta ofZeta) {
		if(playBus == null)
			playBus = new FabricZetaEventBus<>(PlayEvent.class, IZetaPlayEvent.class, null, ofZeta);
		return playBus;
	}

	/**
	 * @return whether anyone listens for this exact play event type. Hooks use this to skip building events.
	 */
	public static boolean hasPlayListeners(Class<? extends IZetaPlayEvent> type) {
		FabricZetaEventBus<IZetaPlayEvent> bus = playBus;
		return bus != null && bus.hasListeners(type);
	}

	public static <T extends IZetaPlayEvent> T firePlay(T event, Class<? super T> firedAs) {
		FabricZetaEventBus<IZetaPlayEvent> bus = playBus;
		if(bus != null)
			bus.fire(event, firedAs);
		return event;
	}

	/**
	 * Fires a load event on every Zeta's own load bus.
	 */
	public static <T extends IZetaLoadEvent> T fireLoadAll(T event, Class<? super T> firedAs) {
		for(Zeta z : ZetaList.INSTANCE.getZetas())
			z.loadBus.fire(event, firedAs);
		return event;
	}
}
