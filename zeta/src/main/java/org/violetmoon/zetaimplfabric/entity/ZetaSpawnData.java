package org.violetmoon.zetaimplfabric.entity;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Replaces Forge's NetworkHooks#getEntitySpawningPacket. Vanilla's add-entity packet is used; for entities with
 * {@link IEntityAdditionalSpawnData}, ServerEntityMixin puts a second packet with their extra data right behind it in
 * the bundle vanilla sends when a player starts tracking the entity, and the client applies it once the entity exists.
 * <p>
 * The two must not be bundled here: vanilla wraps the add-entity packet in its own bundle, and a bundle inside a
 * bundle can't be encoded (players got kicked with "Can't serialize unregistered packet").
 */
public final class ZetaSpawnData {

	public static final ResourceLocation CHANNEL = new ResourceLocation("zeta", "spawn_data");

	private ZetaSpawnData() { }

	public static Packet<ClientGamePacketListener> getEntitySpawningPacket(Entity entity) {
		return new ClientboundAddEntityPacket(entity);
	}

	/**
	 * @return the extra spawn data packet for this entity, or null if it has none
	 */
	@SuppressWarnings("unchecked")
	public static @Nullable Packet<ClientGamePacketListener> getSpawnDataPacket(Entity entity) {
		if(!(entity instanceof IEntityAdditionalSpawnData data))
			return null;

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(entity.getId());
		data.writeSpawnData(buf);
		return (Packet<ClientGamePacketListener>) (Packet<?>) ServerPlayNetworking.createS2CPacket(CHANNEL, buf);
	}
}
