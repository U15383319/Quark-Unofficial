package org.violetmoon.zetaimplfabric.entity;

import net.minecraft.world.entity.MobCategory;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Fabric stand-in for the parts of Forge's IForgeEntity that Quark uses. Injected onto {@link Entity} with Loom
 * interface injection (see fabric.mod.json) and implemented by EntityMixin, so code written against Forge
 * ({@code entity.getPersistentData()}, {@code Player.PERSISTED_NBT_TAG}...) compiles unchanged.
 */
public interface IZetaEntityExtensions {

	/**
	 * Sub-tag of the persistent data that survives player death/respawn (same name as Forge).
	 */
	String PERSISTED_NBT_TAG = "PlayerPersisted";

	/**
	 * Saved with the entity under "ForgeData", like on Forge, so existing data carries over.
	 */
	default CompoundTag getPersistentData() {
		throw new AssertionError("implemented by EntityMixin");
	}

	/**
	 * @return the list item drops are currently being captured into, or null
	 */
	default @Nullable Collection<ItemEntity> captureDrops() {
		throw new AssertionError("implemented by EntityMixin");
	}

	/**
	 * Starts (non-null) or stops (null) capturing item drops spawned through spawnAtLocation.
	 *
	 * @return the previous capture list
	 */
	default @Nullable Collection<ItemEntity> captureDrops(@Nullable Collection<ItemEntity> value) {
		throw new AssertionError("implemented by EntityMixin");
	}

	/**
	 * Forge: Entity#getStepHeight (step height including Forge's STEP_HEIGHT_ADDITION attribute, which Fabric lacks).
	 */
	default float getStepHeight() {
		return ((Entity) this).maxUpStep();
	}

	/**
	 * Forge: IForgeEntity#getClassification, the category an entity counts as for mob caps. Read by NaturalSpawnerMixin.
	 */
	default MobCategory getClassification(boolean forSpawnCount) {
		return ((Entity) this).getType().getCategory();
	}

	/**
	 * Forge's Entity#serializeNBT: the full save tag, including the entity type id.
	 */
	default CompoundTag serializeNBT() {
		Entity self = (Entity) this;
		CompoundTag tag = new CompoundTag();
		String id = self.getEncodeId();
		if(id != null)
			tag.putString("id", id);
		return self.saveWithoutId(tag);
	}

	/**
	 * Forge's Entity#deserializeNBT.
	 */
	default void deserializeNBT(CompoundTag tag) {
		((Entity) this).load(tag);
	}
}
