package org.violetmoon.zetaimplfabric.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.network.IZetaMessage;
import org.violetmoon.zeta.network.IZetaNetworkEventContext;
import org.violetmoon.zeta.network.ZetaHandshakeMessage;
import org.violetmoon.zeta.network.ZetaNetworkDirection;
import org.violetmoon.zeta.network.ZetaNetworkHandler;
import org.violetmoon.zeta.util.ZetaSide;
import org.violetmoon.zetaimplfabric.FabricZeta;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Replaces Forge's SimpleChannel. Play messages share one channel per mod ("modid:main") and are told apart by a
 * leading index byte, assigned in registration order like Forge's discriminators. Login messages use Fabric's login
 * query API on "modid:login".
 */
public class FabricZetaNetworkHandler extends ZetaNetworkHandler {

	final ResourceLocation playChannel;
	final ResourceLocation loginChannel;

	final List<Class<? extends IZetaMessage>> playMessages = new ArrayList<>();
	final Map<Class<?>, Integer> playIds = new HashMap<>();

	private final Map<Integer, LoginEntry<?>> loginById = new HashMap<>();
	private final Map<Class<?>, Integer> loginIds = new HashMap<>();

	private boolean serverPlayHooked = false;
	private boolean clientPlayHooked = false;
	private boolean loginHooked = false;

	record LoginEntry<T extends ZetaHandshakeMessage>(Class<T> clazz, ZetaNetworkDirection dir, @Nullable Function<Boolean, List<Pair<String, T>>> generators) { }

	public FabricZetaNetworkHandler(FabricZeta zeta, int protocolVersion) {
		super(zeta, protocolVersion);
		this.playChannel = new ResourceLocation(zeta.modid, "main");
		this.loginChannel = new ResourceLocation(zeta.modid, "login");
	}

	@Override
	public <T extends IZetaMessage> void register(Class<T> clazz, ZetaNetworkDirection dir) {
		playIds.put(clazz, playMessages.size());
		playMessages.add(clazz);

		if(dir == ZetaNetworkDirection.PLAY_TO_SERVER && !serverPlayHooked) {
			serverPlayHooked = true;
			ServerPlayNetworking.registerGlobalReceiver(playChannel, (server, player, handler, buf, responseSender) -> {
				IZetaMessage msg = read(buf);
				msg.receive(new ServerContext(server::submit, player));
			});
		}

		if(dir == ZetaNetworkDirection.PLAY_TO_CLIENT && !clientPlayHooked && zeta.side == ZetaSide.CLIENT) {
			clientPlayHooked = true;
			FabricZetaClientNetworking.registerPlayReceiver(this);
		}
	}

	@Override
	public <T extends ZetaHandshakeMessage> void registerLogin(Class<T> clazz, ZetaNetworkDirection dir, int id, boolean hasResponse, @Nullable Function<Boolean, List<Pair<String, T>>> loginPacketGenerators) {
		loginById.put(id, new LoginEntry<>(clazz, dir, loginPacketGenerators));
		loginIds.put(clazz, id);

		if(!loginHooked) {
			loginHooked = true;

			// server -> client queries
			ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
				boolean isLocal = handler.connection.isMemoryConnection();
				for(LoginEntry<?> entry : loginById.values()) {
					if(entry.dir() != ZetaNetworkDirection.LOGIN_TO_CLIENT || entry.generators() == null)
						continue;
					for(Pair<String, ? extends ZetaHandshakeMessage> pair : entry.generators().apply(isLocal))
						sender.sendPacket(loginChannel, writeLogin(pair.getRight()));
				}
			});

			// client -> server replies
			ServerLoginNetworking.registerGlobalReceiver(loginChannel, (server, handler, understood, buf, synchronizer, responseSender) -> {
				if(!understood || buf.readableBytes() == 0)
					return;
				ZetaHandshakeMessage reply = readLogin(buf);
				reply.receive(new ServerContext(server::submit, null));
			});

			if(zeta.side == ZetaSide.CLIENT)
				FabricZetaClientNetworking.registerLoginReceiver(this);
		}
	}

	@Override
	public void sendToPlayer(IZetaMessage msg, ServerPlayer player) {
		ServerPlayNetworking.send(player, playChannel, write(msg));
	}

	@Override
	public void sendToServer(IZetaMessage msg) {
		FabricZetaClientNetworking.sendToServer(this, msg);
	}

	@Override
	public Packet<?> wrapInVanilla(IZetaMessage msg, ZetaNetworkDirection dir) {
		if(dir == ZetaNetworkDirection.PLAY_TO_CLIENT)
			return ServerPlayNetworking.createS2CPacket(playChannel, write(msg));
		return FabricZetaClientNetworking.createC2SPacket(this, msg);
	}

	/// serialization ///

	FriendlyByteBuf write(IZetaMessage msg) {
		Integer id = playIds.get(msg.getClass());
		if(id == null)
			throw new IllegalArgumentException("Unregistered message " + msg.getClass().getName() + " on " + playChannel);

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(id);
		serializer.writeObject(msg, buf);
		return buf;
	}

	IZetaMessage read(FriendlyByteBuf buf) {
		int id = buf.readVarInt();
		return serializer.instantiateAndReadObject(playMessages.get(id), buf);
	}

	FriendlyByteBuf writeLogin(ZetaHandshakeMessage msg) {
		Integer id = loginIds.get(msg.getClass());
		if(id == null)
			throw new IllegalArgumentException("Unregistered login message " + msg.getClass().getName() + " on " + loginChannel);

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(id);
		serializer.writeObject(msg, buf);
		return buf;
	}

	ZetaHandshakeMessage readLogin(FriendlyByteBuf buf) {
		int id = buf.readVarInt();
		LoginEntry<?> entry = loginById.get(id);
		if(entry == null)
			throw new IllegalArgumentException("Unknown login message id " + id + " on " + loginChannel);
		return serializer.instantiateAndReadObject(entry.clazz(), buf);
	}

	/// contexts ///

	interface Executor {
		CompletableFuture<Void> submit(Runnable runnable);
	}

	record ServerContext(Executor executor, @Nullable ServerPlayer sender) implements IZetaNetworkEventContext {
		@Override
		public CompletableFuture<Void> enqueueWork(Runnable runnable) {
			return executor.submit(runnable);
		}

		@Override
		public @Nullable ServerPlayer getSender() {
			return sender;
		}

		@Override
		public void reply(ZetaHandshakeMessage msg) {
			throw new UnsupportedOperationException("Replies are only supported for server->client login messages");
		}
	}
}
