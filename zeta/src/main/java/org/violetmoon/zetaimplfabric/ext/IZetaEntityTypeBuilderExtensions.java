package org.violetmoon.zetaimplfabric.ext;

import java.util.function.BiFunction;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Forge's EntityType.Builder additions, injected with Loom interface injection.
 */
public interface IZetaEntityTypeBuilderExtensions<T extends Entity> {

	/**
	 * Forge: setShouldReceiveVelocityUpdates. Stored on the EntityType and read by EntityTypeMixin#trackDeltas.
	 */
	default EntityType.Builder<T> setShouldReceiveVelocityUpdates(boolean value) {
		throw new AssertionError("implemented by EntityTypeBuilderMixin");
	}

	/**
	 * Forge: setCustomClientFactory. Fabric clients create entities through the EntityType's own factory, so this is a
	 * no-op kept for source compatibility.
	 */
	@SuppressWarnings("unchecked")
	default EntityType.Builder<T> setCustomClientFactory(BiFunction<Object, Level, T> factory) {
		return (EntityType.Builder<T>) this;
	}
}
