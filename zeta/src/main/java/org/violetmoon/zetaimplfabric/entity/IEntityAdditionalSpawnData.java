package org.violetmoon.zetaimplfabric.entity;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Fabric stand-in for Forge's net.minecraftforge.entity.IEntityAdditionalSpawnData: extra data sent with the
 * entity's spawn packet. Entities return {@link ZetaSpawnData#getEntitySpawningPacket} from getAddEntityPacket.
 */
public interface IEntityAdditionalSpawnData {
	void writeSpawnData(FriendlyByteBuf buffer);

	void readSpawnData(FriendlyByteBuf additionalData);
}
