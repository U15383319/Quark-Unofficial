package org.violetmoon.zetaimplfabric.client;

import org.violetmoon.zetaimplfabric.entity.IEntityAdditionalSpawnData;
import org.violetmoon.zetaimplfabric.entity.ZetaSpawnData;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Client half of {@link ZetaSpawnData}.
 */
public final class ZetaSpawnDataClient {

	private ZetaSpawnDataClient() { }

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(ZetaSpawnData.CHANNEL, (client, handler, buf, responseSender) -> {
			int id = buf.readVarInt();
			FriendlyByteBuf data = new FriendlyByteBuf(buf.copy());
			client.execute(() -> {
				try {
					if(client.level != null && client.level.getEntity(id) instanceof IEntityAdditionalSpawnData entity)
						entity.readSpawnData(data);
				} finally {
					data.release();
				}
			});
		});
	}
}
