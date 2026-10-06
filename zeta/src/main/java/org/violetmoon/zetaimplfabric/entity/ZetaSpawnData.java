package org.violetmoon.zetaimplfabric.entity;

import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Replaces Forge's NetworkHooks#getEntitySpawningPacket. Vanilla's add-entity packet is used; entities with
 * {@link IEntityAdditionalSpawnData} get it bundled with a second packet carrying their extra data, which the client
 * applies right after the entity is created.
 */
public final class ZetaSpawnData {

	public static final ResourceLocation CHANNEL = new ResourceLocation("zeta", "spawn_data");

	private ZetaSpawnData() { }

	@SuppressWarnings("unchecked")
	public static Packet<ClientGamePacketListener> getEntitySpawningPacket(Entity entity) {
		ClientboundAddEntityPacket add = new ClientboundAddEntityPacket(entity);
		if(!(entity instanceof IEntityAdditionalSpawnData data))
			return add;

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(entity.getId());
		data.writeSpawnData(buf);
		Packet<ClientGamePacketListener> extra = (Packet<ClientGamePacketListener>) (Packet<?>) ServerPlayNetworking.createS2CPacket(CHANNEL, buf);
		return new ClientboundBundlePacket(List.of(add, extra));
	}
}
