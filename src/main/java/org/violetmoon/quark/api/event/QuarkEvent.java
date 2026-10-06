package org.violetmoon.quark.api.event;

/**
 * Fabric: base of Quark's API events (on Forge they extended Forge's Event and were posted on MinecraftForge.EVENT_BUS).
 * Listen through {@link QuarkApiEvents}.
 */
public abstract class QuarkEvent {

	private boolean canceled = false;

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean cancel) {
		this.canceled = cancel;
	}
}
