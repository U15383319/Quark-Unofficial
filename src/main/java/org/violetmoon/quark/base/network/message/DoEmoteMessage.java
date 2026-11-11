package org.violetmoon.quark.base.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.violetmoon.quark.content.tweaks.client.emote.EmoteHandler;
import org.violetmoon.zeta.network.IZetaMessage;
import org.violetmoon.zeta.network.IZetaNetworkEventContext;

import java.io.Serial;
import java.util.UUID;

public class DoEmoteMessage implements IZetaMessage {

	@Serial
	private static final long serialVersionUID = -7952633556330869633L;

	public String emote;
	public UUID playerUUID;
	public int tier;

	public DoEmoteMessage() {}

	public DoEmoteMessage(String emote, UUID playerUUID, int tier) {
		this.emote = emote;
		this.playerUUID = playerUUID;
		this.tier = tier;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean receive(IZetaNetworkEventContext context) {
		context.enqueueWork(() -> Client.receive(this));

		return true;
	}

	// Fabric strips @Environment(CLIENT) methods on a dedicated server, but not the lambdas inside them, and a lambda
	// using client classes keeps the whole message class from loading there (it could never be sent). So the client
	// code lives in a class of its own, only loaded when it runs.
	@Environment(EnvType.CLIENT)
	private static final class Client {
		static void receive(DoEmoteMessage msg) {
			Level world = Minecraft.getInstance().level;
			Player player = world.getPlayerByUUID(msg.playerUUID);
			EmoteHandler.putEmote(player, msg.emote, msg.tier);
		}
	}

}
