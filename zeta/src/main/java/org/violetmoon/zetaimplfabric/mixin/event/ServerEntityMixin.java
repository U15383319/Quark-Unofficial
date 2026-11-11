package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.entity.ZetaSpawnData;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

/**
 * Sends an entity's extra spawn data (IEntityAdditionalSpawnData) right after its add-entity packet, in the same
 * bundle (see ZetaSpawnData).
 */
@Mixin(ServerEntity.class)
public class ServerEntityMixin {

	@Shadow @Final private Entity entity;

	@WrapOperation(method = "sendPairingData", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
	private void zeta$spawnData(Consumer<Packet<ClientGamePacketListener>> consumer, Object addEntity, Operation<Void> original) {
		original.call(consumer, addEntity);
		Packet<ClientGamePacketListener> extra = ZetaSpawnData.getSpawnDataPacket(entity);
		if(extra != null)
			consumer.accept(extra);
	}
}
