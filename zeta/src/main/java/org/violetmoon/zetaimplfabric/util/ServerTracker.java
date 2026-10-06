package org.violetmoon.zetaimplfabric.util;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric replacement for Forge's ServerLifecycleHooks#getCurrentServer.
 */
public final class ServerTracker {

	private static @Nullable MinecraftServer currentServer;

	private ServerTracker() { }

	public static void init() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			if(currentServer == server)
				currentServer = null;
		});
	}

	public static @Nullable MinecraftServer getCurrentServer() {
		return currentServer;
	}
}
