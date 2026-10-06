package org.violetmoon.zetaimplfabric.mixin.ext;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.ext.IZetaEntityTypeBuilderExtensions;
import org.violetmoon.zetaimplfabric.ext.IZetaEntityTypeExtensions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityType.Builder.class)
public abstract class EntityTypeBuilderExtMixin<T extends Entity> implements IZetaEntityTypeBuilderExtensions<T> {

	@Unique
	private @Nullable Boolean zeta$trackDeltas;

	@SuppressWarnings("unchecked")
	@Override
	public EntityType.Builder<T> setShouldReceiveVelocityUpdates(boolean value) {
		zeta$trackDeltas = value;
		return (EntityType.Builder<T>) (Object) this;
	}

	@Inject(method = "build", at = @At("RETURN"))
	private void zeta$applyTrackDeltas(String key, CallbackInfoReturnable<EntityType<T>> cir) {
		if(zeta$trackDeltas != null)
			((IZetaEntityTypeExtensions) cir.getReturnValue()).zeta$setTrackDeltas(zeta$trackDeltas);
	}
}
