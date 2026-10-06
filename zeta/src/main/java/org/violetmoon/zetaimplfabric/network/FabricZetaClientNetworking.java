package org.violetmoon.zetaimplfabric.network;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.network.IZetaMessage;
import org.violetmoon.zeta.network.IZetaNetworkEventContext;
import org.violetmoon.zeta.network.ZetaHandshakeMessage;

import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client-only half of {@link FabricZetaNetworkHandler}. Only touched when running on the physical client.
 */
final class FabricZetaClientNetworking {

	private FabricZetaClientNetworking() { }

	static void registerPlayReceiver(FabricZetaNetworkHandler handler) {
		ClientPlayNetworking.registerGlobalReceiver(handler.playChannel, (client, listener, buf, responseSender) -> {
			IZetaMessage msg = handler.read(buf);
			msg.receive(new ClientContext(client, null));
		});
	}

	static void registerLoginReceiver(FabricZetaNetworkHandler handler) {
		ClientLoginNetworking.registerGlobalReceiver(handler.loginChannel, (client, listener, buf, listenerAdder) -> {
			ZetaHandshakeMessage msg = handler.readLogin(buf);
			ClientContext context = new ClientContext(client, handler);
			msg.receive(context);
			return CompletableFuture.completedFuture(context.reply);
		});
	}

	static void sendToServer(FabricZetaNetworkHandler handler, IZetaMessage msg) {
		ClientPlayNetworking.send(handler.playChannel, handler.write(msg));
	}

	static Packet<?> createC2SPacket(FabricZetaNetworkHandler handler, IZetaMessage msg) {
		return ClientPlayNetworking.createC2SPacket(handler.playChannel, handler.write(msg));
	}

	private static final class ClientContext implements IZetaNetworkEventContext {
		private final Minecraft client;
		private final @Nullable FabricZetaNetworkHandler loginHandler;
		private @Nullable FriendlyByteBuf reply;

		ClientContext(Minecraft client, @Nullable FabricZetaNetworkHandler loginHandler) {
			this.client = client;
			this.loginHandler = loginHandler;
		}

		@Override
		public CompletableFuture<Void> enqueueWork(Runnable runnable) {
			return client.submit(runnable);
		}

		@Override
		public @Nullable ServerPlayer getSender() {
			return null;
		}

		@Override
		public void reply(ZetaHandshakeMessage msg) {
			if(loginHandler == null)
				throw new UnsupportedOperationException("Replies are only supported for login messages");
			reply = loginHandler.writeLogin(msg);
		}
	}
}
