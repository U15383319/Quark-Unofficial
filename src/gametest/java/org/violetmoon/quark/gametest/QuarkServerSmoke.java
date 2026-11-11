package org.violetmoon.quark.gametest;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * Server side of the multiplayer smoke run (off unless -Dquark.smoke.server=true): makes joining players operators,
 * so QuarkClientSmoke can set up its scene with commands, and stops the server once the last player has left.
 */
public class QuarkServerSmoke implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		if(!Boolean.getBoolean("quark.smoke.server"))
			return;

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.getPlayerList().op(handler.getPlayer().getGameProfile());
			System.out.println("QUARK_SMOKE_SERVER op " + handler.getPlayer().getGameProfile().getName());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> server.execute(() -> {
			if(server.getPlayerList().getPlayerCount() <= 1) {
				System.out.println("QUARK_SMOKE_SERVER last player left, stopping");
				server.halt(false);
			}
		}));

		// never outlive a test run, even if the client never connects or never leaves
		int[] ticks = { 0 };
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if(++ticks[0] == 20 * 300) {
				System.out.println("QUARK_SMOKE_SERVER time limit reached, stopping");
				server.halt(false);
			}
		});
	}
}
