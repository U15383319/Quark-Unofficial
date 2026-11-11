package org.violetmoon.quark.base.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.violetmoon.zeta.network.IZetaMessage;
import org.violetmoon.zeta.network.IZetaNetworkEventContext;

import java.io.Serial;

public class UpdateTridentMessage implements IZetaMessage {

	@Serial
	private static final long serialVersionUID = -4716987873031723456L;

	public int tridentID;
	public ItemStack stack;

	public UpdateTridentMessage() {}

	public UpdateTridentMessage(int trident, ItemStack stack) {
		this.tridentID = trident;
		this.stack = stack;
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
		static void receive(UpdateTridentMessage msg) {
			Level level = Minecraft.getInstance().level;
			if(level != null) {
				Entity entity = level.getEntity(msg.tridentID);
				if(entity instanceof ThrownTrident trident) {
					trident.tridentItem = msg.stack;
				}
			}
		}
	}

}
