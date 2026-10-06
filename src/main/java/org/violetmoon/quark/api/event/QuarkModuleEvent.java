package org.violetmoon.quark.api.event;


public class QuarkModuleEvent extends QuarkEvent {

	public final String eventName;

	public QuarkModuleEvent(String eventName) {
		this.eventName = eventName;
	}

}
