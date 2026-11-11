package org.violetmoon.quark.gametest;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.server.MinecraftServer;

/**
 * Client smoke run (off unless -Dquark.smoke=true): once the player is in a world, runs the commands in
 * -Dquark.smoke.commands (separated by "|", run by the integrated server as the player, with operator rights), waits
 * for the chunks to render, saves screenshots/quark-smoke-N.png after each "|shot" and quits. Used with
 * runProductionSmoke to look at Quark's rendering next to Sodium and Iris.
 */
public class QuarkClientSmoke implements ClientModInitializer {

	private static final int SETTLE_TICKS = 100;

	private final List<String> steps = new ArrayList<>();
	private int step = 0;
	private int wait = 0;
	private int shots = 0;

	@Override
	public void onInitializeClient() {
		if(!Boolean.getBoolean("quark.smoke"))
			return;

		for(String s : System.getProperty("quark.smoke.commands", "").split("\\|"))
			if(!s.isBlank())
				steps.add(s.trim());
		if(steps.isEmpty() || !steps.get(steps.size() - 1).equals("shot"))
			steps.add("shot");

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft mc) {
		if(mc.player == null || mc.level == null || mc.getSingleplayerServer() == null)
			return;
		// screenshots without HUD, chat or the tutorial toast
		mc.options.hideGui = true;
		mc.getTutorial().setStep(TutorialSteps.NONE);
		if(wait > 0) {
			wait--;
			return;
		}
		if(step >= steps.size()) {
			System.out.println("QUARK_SMOKE done, " + shots + " screenshot(s)");
			mc.stop();
			return;
		}

		String s = steps.get(step++);
		if(s.equals("shot")) {
			String name = "quark-smoke-" + (++shots) + ".png";
			Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> System.out.println("QUARK_SMOKE " + msg.getString()));
			wait = 5;
		} else if(s.startsWith("wait ")) {
			wait = Integer.parseInt(s.substring(5).trim());
		} else {
			MinecraftServer server = mc.getSingleplayerServer();
			var player = server.getPlayerList().getPlayer(mc.player.getUUID());
			String cmd = s.startsWith("/") ? s.substring(1) : s;
			server.execute(() -> {
				int result = server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4), cmd);
				System.out.println("QUARK_SMOKE ran '" + cmd + "' -> " + result);
			});
			// after the first command, give the world time to load and render before going on
			wait = step == 1 ? SETTLE_TICKS : 10;
		}
	}
}
