package org.violetmoon.quark.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.network.message.RequestEmoteMessage;
import org.violetmoon.quark.base.network.message.SortInventoryMessage;
import org.violetmoon.quark.content.tweaks.client.emote.EmoteHandler;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client smoke run (off unless -Dquark.smoke=true), in singleplayer or connected to a server. Once the player is in
 * a world it runs the steps in -Dquark.smoke.commands, separated by "|":
 * <ul>
 * <li>a command ("/..."; in singleplayer run by the integrated server with operator rights, on a server sent as the
 * player, whom QuarkServerSmoke makes an operator)</li>
 * <li>"wait N" ticks; "shot" saves screenshots/quark-smoke-N.png; "camera first|back|front" switches the view; "sneak on|off"</li>
 * <li>"sort": Quark's sort-inventory packet; "emote NAME": Quark's emote request packet;
 * "use X Y Z": right-clicks the block with the main hand, as a player would</li>
 * <li>checks of what this client sees, retried for up to 3 seconds: "expect-inv SLOT ITEM|empty|nonempty",
 * "expect-block X Y Z BLOCK [PROPERTY=VALUE]", "expect-entity ENTITY X Y Z", "expect-emote"</li>
 * </ul>
 * Results are logged as "QUARK_SMOKE PASS/FAIL ..."; then it quits. Used with runProductionSmoke and
 * runProductionMultiplayer to look at Quark next to Sodium and Iris, and over a real server connection.
 */
public class QuarkClientSmoke implements ClientModInitializer {

	private static final int SETTLE_TICKS = 100;
	private static final int EXPECT_TICKS = 60;

	private final List<String> steps = new ArrayList<>();
	private int step = 0;
	private int wait = 0;
	private int shots = 0;
	private int passed = 0;
	private int failed = 0;
	private int expectTicks = 0;
	private boolean firstStep = true;

	@Override
	public void onInitializeClient() {
		if(!Boolean.getBoolean("quark.smoke"))
			return;

		for(String s : System.getProperty("quark.smoke.commands", "").split("\\|"))
			if(!s.isBlank())
				steps.add(s.trim());
		if(steps.stream().noneMatch(s -> s.equals("shot") || s.startsWith("expect")))
			steps.add("shot");

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private boolean joined = false;

	private int ticksWithoutWorld = 0;

	private void tick(Minecraft mc) {
		if(!joined && ++ticksWithoutWorld == 20 * 150) {
			result(false, "never got into a world (stuck at " + (mc.screen == null ? "no screen" : mc.screen.getClass().getSimpleName()) + ")");
			System.out.println("QUARK_SMOKE done: " + passed + " passed, " + failed + " failed, " + shots + " screenshot(s)");
			mc.stop();
			return;
		}
		if(joined && mc.screen instanceof DisconnectedScreen) {
			result(false, "disconnected from the server (see the server log)");
			System.out.println("QUARK_SMOKE done: " + passed + " passed, " + failed + " failed, " + shots + " screenshot(s)");
			mc.stop();
			return;
		}
		if(mc.player == null || mc.level == null || mc.getConnection() == null)
			return;
		joined = true;
		// screenshots without HUD, chat or the tutorial toast
		mc.options.hideGui = true;
		mc.getTutorial().setStep(TutorialSteps.NONE);

		if(firstStep) {
			// let the world load (and the server make us an operator)
			firstStep = false;
			wait = SETTLE_TICKS;
		}
		if(wait > 0) {
			wait--;
			return;
		}
		if(step >= steps.size()) {
			System.out.println("QUARK_SMOKE done: " + passed + " passed, " + failed + " failed, " + shots + " screenshot(s)");
			mc.stop();
			return;
		}

		String s = steps.get(step);
		String[] a = s.split("\\s+");
		try {
			if(s.startsWith("expect")) {
				BooleanSupplier check = expectation(mc, a);
				if(check.getAsBoolean()) {
					result(true, s);
				} else if(++expectTicks >= EXPECT_TICKS) {
					result(false, s + " (saw: " + describe(mc, a) + ")");
				} else
					return; // try again next tick
				expectTicks = 0;
				step++;
				return;
			}

			step++;
			switch(a[0]) {
				case "shot" -> {
					String name = "quark-smoke-" + (++shots) + ".png";
					Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> System.out.println("QUARK_SMOKE " + msg.getString()));
					wait = 5;
				}
				case "wait" -> wait = Integer.parseInt(a[1]);
				case "sneak" -> mc.options.keyShift.setDown(a[1].equals("on"));
				case "camera" -> mc.options.setCameraType(switch(a[1]) {
					case "back" -> CameraType.THIRD_PERSON_BACK;
					case "front" -> CameraType.THIRD_PERSON_FRONT;
					default -> CameraType.FIRST_PERSON;
				});
				case "sort" -> {
					QuarkClient.ZETA_CLIENT.sendToServer(new SortInventoryMessage(true));
					System.out.println("QUARK_SMOKE sent sort");
				}
				case "emote" -> {
					QuarkClient.ZETA_CLIENT.sendToServer(new RequestEmoteMessage(a[1]));
					System.out.println("QUARK_SMOKE sent emote " + a[1]);
				}
				case "use" -> {
					BlockPos pos = new BlockPos(Integer.parseInt(a[1]), Integer.parseInt(a[2]), Integer.parseInt(a[3]));
					var result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
					System.out.println("QUARK_SMOKE used " + pos.toShortString() + " -> " + result);
					wait = 5;
				}
				default -> runCommand(mc, s);
			}
		} catch(Exception e) {
			result(false, s + " threw " + e);
			step = Math.max(step, steps.indexOf(s) + 1);
		}
	}

	private void runCommand(Minecraft mc, String s) {
		String cmd = s.startsWith("/") ? s.substring(1) : s;
		MinecraftServer server = mc.getSingleplayerServer();
		if(server != null) {
			var player = server.getPlayerList().getPlayer(mc.player.getUUID());
			server.execute(() -> {
				int result = server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4), cmd);
				System.out.println("QUARK_SMOKE ran '" + cmd + "' -> " + result);
			});
		} else {
			mc.player.connection.sendCommand(cmd);
			System.out.println("QUARK_SMOKE sent '" + cmd + "'");
		}
		wait = 10;
	}

	private BooleanSupplier expectation(Minecraft mc, String[] a) {
		return switch(a[0]) {
			case "expect-inv" -> () -> {
				ItemStack stack = mc.player.getInventory().getItem(Integer.parseInt(a[1]));
				if(a[2].equals("empty"))
					return stack.isEmpty();
				if(a[2].equals("nonempty"))
					return !stack.isEmpty();
				return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(new ResourceLocation(a[2]));
			};
			case "expect-block" -> () -> {
				BlockState state = mc.level.getBlockState(new BlockPos(Integer.parseInt(a[1]), Integer.parseInt(a[2]), Integer.parseInt(a[3])));
				if(!BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(new ResourceLocation(a[4])))
					return false;
				if(a.length < 6)
					return true;
				String[] kv = a[5].split("=");
				return state.getProperties().stream().anyMatch(p -> p.getName().equals(kv[0]) && state.getValue(p).toString().equals(kv[1]));
			};
			case "expect-entity" -> () -> !entities(mc, a).isEmpty();
			case "expect-emote" -> () -> EmoteHandler.getPlayerEmote(mc.player) != null;
			default -> throw new IllegalArgumentException("unknown check " + a[0]);
		};
	}

	private List<Entity> entities(Minecraft mc, String[] a) {
		var type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(a[1]));
		Vec3 at = new Vec3(Double.parseDouble(a[2]), Double.parseDouble(a[3]), Double.parseDouble(a[4]));
		return mc.level.getEntities((Entity) null, new AABB(at, at).inflate(4), e -> e.getType() == type);
	}

	private String describe(Minecraft mc, String[] a) {
		return switch(a[0]) {
			case "expect-inv" -> mc.player.getInventory().getItem(Integer.parseInt(a[1])).toString();
			case "expect-block" -> mc.level.getBlockState(new BlockPos(Integer.parseInt(a[1]), Integer.parseInt(a[2]), Integer.parseInt(a[3]))).toString();
			case "expect-entity" -> entities(mc, a).size() + " matching";
			case "expect-emote" -> "no emote";
			default -> "?";
		};
	}

	private void result(boolean ok, String what) {
		if(ok)
			passed++;
		else
			failed++;
		System.out.println("QUARK_SMOKE " + (ok ? "PASS " : "FAIL ") + what);
	}
}
