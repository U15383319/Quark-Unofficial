package org.violetmoon.zeta.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabric: stand-in for Forge's net.minecraftforge.common.ToolAction.
 * A named, interned action an item can perform. Compare by identity.
 */
public final class ToolAction {

	private static final Map<String, ToolAction> ACTIONS = new ConcurrentHashMap<>();

	private final String name;

	private ToolAction(String name) {
		this.name = name;
	}

	public static ToolAction get(String name) {
		return ACTIONS.computeIfAbsent(name, ToolAction::new);
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "ToolAction[" + name + "]";
	}
}
