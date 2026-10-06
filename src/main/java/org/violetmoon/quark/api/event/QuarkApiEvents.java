package org.violetmoon.quark.api.event;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Fabric: where other mods listen for Quark's API events (Forge: MinecraftForge.EVENT_BUS).
 */
public final class QuarkApiEvents {

	public static final Event<Consumer<SimpleHarvestEvent>> SIMPLE_HARVEST = create();
	public static final Event<Consumer<GatherToolClassesEvent>> GATHER_TOOL_CLASSES = create();
	public static final Event<Consumer<ModuleStateChangedEvent>> MODULE_STATE_CHANGED = create();
	/** Client only. */
	public static final Event<Consumer<UsageTickerEvent.GetStack>> USAGE_TICKER_GET_STACK = create();
	/** Client only. */
	public static final Event<Consumer<UsageTickerEvent.GetCount>> USAGE_TICKER_GET_COUNT = create();

	private QuarkApiEvents() { }

	@SuppressWarnings("unchecked")
	private static <T> Event<Consumer<T>> create() {
		return EventFactory.createArrayBacked(Consumer.class, listeners -> event -> {
			for(Consumer<T> listener : listeners)
				listener.accept(event);
		});
	}
}
