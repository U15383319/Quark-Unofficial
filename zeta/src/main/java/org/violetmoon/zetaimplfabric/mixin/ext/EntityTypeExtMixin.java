package org.violetmoon.zetaimplfabric.mixin.ext;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.ext.IZetaEntityTypeExtensions;

import net.minecraft.world.entity.EntityType;

@Mixin(EntityType.class)
public abstract class EntityTypeExtMixin implements IZetaEntityTypeExtensions {

	@Unique
	private @Nullable Boolean zeta$trackDeltas;

	@Override
	public void zeta$setTrackDeltas(Boolean value) {
		zeta$trackDeltas = value;
	}

	// Forge: EntityType#shouldReceiveVelocityUpdates replaces trackDeltas
	@Inject(method = "trackDeltas", at = @At("HEAD"), cancellable = true)
	private void zeta$trackDeltas(CallbackInfoReturnable<Boolean> cir) {
		if(zeta$trackDeltas != null)
			cir.setReturnValue(zeta$trackDeltas);
	}
}
