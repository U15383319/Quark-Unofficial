package org.violetmoon.zetaimplfabric.event;

import org.violetmoon.zeta.event.bus.ZResult;

import net.minecraft.world.InteractionResult;

/**
 * Common state of Forge's Event: cancellation, result, and (for interaction events) the cancellation result.
 * Subclasses only declare the Zeta interfaces they implement; these methods satisfy Cancellable/Resultable.
 */
public abstract class FabricZEvent {

	private boolean canceled = false;
	private ZResult result = ZResult.DEFAULT;
	private InteractionResult cancellationResult = InteractionResult.PASS;

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean cancel) {
		this.canceled = cancel;
	}

	public ZResult getResult() {
		return result;
	}

	public void setResult(ZResult value) {
		this.result = value;
	}

	public InteractionResult getCancellationResult() {
		return cancellationResult;
	}

	public void setCancellationResult(InteractionResult result) {
		this.cancellationResult = result;
	}
}
