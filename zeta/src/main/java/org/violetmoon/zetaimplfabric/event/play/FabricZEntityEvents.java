package org.violetmoon.zetaimplfabric.event.play;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.event.bus.ZResult;
import org.violetmoon.zeta.event.play.entity.ZEntityJoinLevel;
import org.violetmoon.zeta.event.play.entity.ZEntityMobGriefing;
import org.violetmoon.zeta.event.play.entity.ZEntityTeleport;
import org.violetmoon.zeta.event.play.entity.living.ZAnimalTame;
import org.violetmoon.zeta.event.play.entity.living.ZBabyEntitySpawn;
import org.violetmoon.zeta.event.play.entity.living.ZLivingChangeTarget;
import org.violetmoon.zeta.event.play.entity.living.ZLivingConversion;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDeath;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDrops;
import org.violetmoon.zeta.event.play.entity.living.ZLivingFall;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.event.play.entity.living.ZMobSpawnEvent;
import org.violetmoon.zeta.event.play.entity.living.ZSleepingLocationCheck;
import org.violetmoon.zetaimplfabric.event.FabricZEvent;
import org.violetmoon.zetaimplfabric.event.FabricZetaEvents;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Entity and living-entity Zeta play events. Fired from mixins in {@code org.violetmoon.zetaimplfabric.mixin}.
 */
public final class FabricZEntityEvents {

	private FabricZEntityEvents() { }

	public static void init() {
		// Forge SleepingLocationCheckEvent (LivingEntity#checkBedExists)
		EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> {
			if(!FabricZetaEvents.hasPlayListeners(ZSleepingLocationCheck.class))
				return InteractionResult.PASS;

			SleepingLocationCheck event = new SleepingLocationCheck(entity, sleepingPos);
			FabricZetaEvents.firePlay(event, ZSleepingLocationCheck.class);
			return switch(event.getResult()) {
				case ALLOW -> InteractionResult.SUCCESS;
				case DENY -> InteractionResult.FAIL;
				default -> InteractionResult.PASS;
			};
		});
	}

	public static void fireLivingTick(LivingEntity entity) {
		FabricZetaEvents.firePlay((ZLivingTick) () -> entity, ZLivingTick.class);
	}

	public static void fireLivingDeath(LivingEntity entity, DamageSource source) {
		LivingDeath event = new LivingDeath(entity, source);
		FabricZetaEvents.firePlay(event, ZLivingDeath.class);
		FabricZetaEvents.firePlay(event, ZLivingDeath.Lowest.class);
	}

	/**
	 * @return true if the drops should be spawned
	 */
	public static boolean fireLivingDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
		LivingDrops event = new LivingDrops(entity, source, drops, lootingLevel, recentlyHit);
		FabricZetaEvents.firePlay(event, ZLivingDrops.class);
		// Lowest listeners (totem of holding) check isCanceled themselves
		FabricZetaEvents.firePlay(event, ZLivingDrops.Lowest.class);
		return !event.isCanceled();
	}

	public static float fireLivingFall(LivingEntity entity, float distance) {
		LivingFall event = new LivingFall(entity, distance);
		FabricZetaEvents.firePlay(event, ZLivingFall.class);
		return event.distance;
	}

	/**
	 * @return true if the target change should go ahead
	 */
	public static boolean fireChangeTarget(LivingEntity entity, @Nullable LivingEntity newTarget, ZLivingChangeTarget.LivingTargetType type) {
		LivingChangeTarget event = new LivingChangeTarget(entity, newTarget, type);
		FabricZetaEvents.firePlay(event, ZLivingChangeTarget.class);
		return !event.isCanceled();
	}

	/**
	 * @return true if the conversion should go ahead
	 */
	public static boolean fireConversionPre(LivingEntity entity, EntityType<? extends LivingEntity> outcome) {
		ConversionPre event = new ConversionPre(entity, outcome);
		FabricZetaEvents.firePlay(event, ZLivingConversion.Pre.class);
		return !event.isCanceled();
	}

	public static void fireConversionPost(LivingEntity entity, LivingEntity outcome) {
		FabricZetaEvents.firePlay(new ConversionPost(entity, outcome), ZLivingConversion.Post.class);
	}

	public static void fireAnimalTame(Animal animal, Player tamer) {
		FabricZetaEvents.firePlay(new ZAnimalTame() {
			@Override
			public Animal getAnimal() {
				return animal;
			}

			@Override
			public Player getTamer() {
				return tamer;
			}
		}, ZAnimalTame.class);
	}

	/**
	 * @return the child to spawn, or null if the breeding was canceled
	 */
	public static @Nullable AgeableMob fireBabySpawn(Mob parentA, Mob parentB, @Nullable Player cause, @Nullable AgeableMob child) {
		BabyEntitySpawn event = new BabyEntitySpawn(parentA, parentB, cause, child);
		FabricZetaEvents.firePlay(event, ZBabyEntitySpawn.class);
		FabricZetaEvents.firePlay(event, ZBabyEntitySpawn.Lowest.class);
		return event.isCanceled() ? null : event.child;
	}

	/**
	 * @return true if the entity may join the level
	 */
	public static boolean fireJoinLevel(Entity entity) {
		EntityJoinLevel event = new EntityJoinLevel(entity);
		FabricZetaEvents.firePlay(event, ZEntityJoinLevel.class);
		return !event.isCanceled();
	}

	/**
	 * Replacement for {@code level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)} at an entity's call site.
	 */
	public static boolean fireMobGriefing(boolean gameRuleValue, @Nullable Entity entity) {
		if(!FabricZetaEvents.hasPlayListeners(ZEntityMobGriefing.class))
			return gameRuleValue;

		MobGriefing event = new MobGriefing(entity);
		FabricZetaEvents.firePlay(event, ZEntityMobGriefing.class);
		return switch(event.getResult()) {
			case ALLOW -> true;
			case DENY -> false;
			default -> gameRuleValue;
		};
	}

	/**
	 * @return the teleport event, holding the (possibly changed) target position
	 */
	public static EntityTeleport fireTeleport(Entity entity, double x, double y, double z) {
		EntityTeleport event = new EntityTeleport(entity, x, y, z);
		FabricZetaEvents.firePlay(event, ZEntityTeleport.class);
		return event;
	}

	/**
	 * Forge MobSpawnEvent.FinalizeSpawn.
	 *
	 * @return the result; DENY means the mob must not be spawned
	 */
	public static ZResult fireCheckSpawn(Mob mob, ServerLevelAccessor level, double x, double y, double z, @Nullable BaseSpawner spawner, MobSpawnType type) {
		CheckSpawn event = new CheckSpawn(mob, level, x, y, z, spawner, type);
		FabricZetaEvents.firePlay(event, ZMobSpawnEvent.CheckSpawn.class);
		FabricZetaEvents.firePlay(event, ZMobSpawnEvent.CheckSpawn.Lowest.class);
		return event.getResult();
	}

	/// event classes ///

	public record LivingDeath(Entity getEntity, DamageSource getSource) implements ZLivingDeath.Lowest { }

	public static class LivingDrops extends FabricZEvent implements ZLivingDrops.Lowest {
		private final LivingEntity entity;
		private final DamageSource source;
		private final Collection<ItemEntity> drops;
		private final int lootingLevel;
		private final boolean recentlyHit;

		public LivingDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit) {
			this.entity = entity;
			this.source = source;
			this.drops = drops;
			this.lootingLevel = lootingLevel;
			this.recentlyHit = recentlyHit;
		}

		@Override
		public LivingEntity getEntity() {
			return entity;
		}

		@Override
		public DamageSource getSource() {
			return source;
		}

		@Override
		public Collection<ItemEntity> getDrops() {
			return drops;
		}

		@Override
		public int getLootingLevel() {
			return lootingLevel;
		}

		@Override
		public boolean isRecentlyHit() {
			return recentlyHit;
		}
	}

	public static class LivingFall implements ZLivingFall {
		private final LivingEntity entity;
		private float distance;

		public LivingFall(LivingEntity entity, float distance) {
			this.entity = entity;
			this.distance = distance;
		}

		@Override
		public LivingEntity getEntity() {
			return entity;
		}

		@Override
		public float getDistance() {
			return distance;
		}

		@Override
		public void setDistance(float distance) {
			this.distance = distance;
		}
	}

	public static class LivingChangeTarget extends FabricZEvent implements ZLivingChangeTarget {
		private final LivingEntity entity;
		private final @Nullable LivingEntity newTarget;
		private final LivingTargetType type;

		public LivingChangeTarget(LivingEntity entity, @Nullable LivingEntity newTarget, LivingTargetType type) {
			this.entity = entity;
			this.newTarget = newTarget;
			this.type = type;
		}

		@Override
		public LivingEntity getEntity() {
			return entity;
		}

		@Override
		public @Nullable LivingEntity getNewTarget() {
			return newTarget;
		}

		@Override
		public LivingTargetType getTargetType() {
			return type;
		}
	}

	public static class ConversionPre extends FabricZEvent implements ZLivingConversion.Pre {
		private final LivingEntity entity;
		private final EntityType<? extends LivingEntity> outcome;

		public ConversionPre(LivingEntity entity, EntityType<? extends LivingEntity> outcome) {
			this.entity = entity;
			this.outcome = outcome;
		}

		@Override
		public LivingEntity getEntity() {
			return entity;
		}

		@Override
		public EntityType<? extends LivingEntity> getOutcome() {
			return outcome;
		}
	}

	public static class ConversionPost extends FabricZEvent implements ZLivingConversion.Post {
		private final LivingEntity entity;
		private final LivingEntity outcome;

		public ConversionPost(LivingEntity entity, LivingEntity outcome) {
			this.entity = entity;
			this.outcome = outcome;
		}

		@Override
		public LivingEntity getEntity() {
			return entity;
		}

		@Override
		public LivingEntity getOutcome() {
			return outcome;
		}
	}

	public static class BabyEntitySpawn extends FabricZEvent implements ZBabyEntitySpawn.Lowest {
		private final Mob parentA;
		private final Mob parentB;
		private final @Nullable Player cause;
		private @Nullable AgeableMob child;

		public BabyEntitySpawn(Mob parentA, Mob parentB, @Nullable Player cause, @Nullable AgeableMob child) {
			this.parentA = parentA;
			this.parentB = parentB;
			this.cause = cause;
			this.child = child;
		}

		@Override
		public Mob getParentA() {
			return parentA;
		}

		@Override
		public Mob getParentB() {
			return parentB;
		}

		@Override
		public @Nullable Player getCausedByPlayer() {
			return cause;
		}

		@Override
		public @Nullable AgeableMob getChild() {
			return child;
		}

		@Override
		public void setChild(AgeableMob proposedChild) {
			this.child = proposedChild;
		}
	}

	public static class EntityJoinLevel extends FabricZEvent implements ZEntityJoinLevel {
		private final Entity entity;

		public EntityJoinLevel(Entity entity) {
			this.entity = entity;
		}

		@Override
		public Entity getEntity() {
			return entity;
		}
	}

	public static class MobGriefing extends FabricZEvent implements ZEntityMobGriefing {
		private final @Nullable Entity entity;

		public MobGriefing(@Nullable Entity entity) {
			this.entity = entity;
		}

		@Override
		public @Nullable Entity getEntity() {
			return entity;
		}
	}

	public static class EntityTeleport extends FabricZEvent implements ZEntityTeleport {
		private final Entity entity;
		private double x, y, z;

		public EntityTeleport(Entity entity, double x, double y, double z) {
			this.entity = entity;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public Entity getEntity() {
			return entity;
		}

		@Override
		public double getTargetX() {
			return x;
		}

		@Override
		public double getTargetY() {
			return y;
		}

		@Override
		public double getTargetZ() {
			return z;
		}

		@Override
		public void setTargetX(double targetX) {
			this.x = targetX;
		}

		@Override
		public void setTargetY(double targetY) {
			this.y = targetY;
		}

		@Override
		public void setTargetZ(double targetZ) {
			this.z = targetZ;
		}
	}

	public static class CheckSpawn extends FabricZEvent implements ZMobSpawnEvent.CheckSpawn.Lowest {
		private final Mob mob;
		private final ServerLevelAccessor level;
		private final double x, y, z;
		private final @Nullable BaseSpawner spawner;
		private final MobSpawnType type;

		public CheckSpawn(Mob mob, ServerLevelAccessor level, double x, double y, double z, @Nullable BaseSpawner spawner, MobSpawnType type) {
			this.mob = mob;
			this.level = level;
			this.x = x;
			this.y = y;
			this.z = z;
			this.spawner = spawner;
			this.type = type;
		}

		@Override
		public Mob getEntity() {
			return mob;
		}

		@Override
		public ServerLevelAccessor getLevel() {
			return level;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}

		@Override
		public double getZ() {
			return z;
		}

		@Override
		public @Nullable BaseSpawner getSpawner() {
			return spawner;
		}

		@Override
		public MobSpawnType getSpawnType() {
			return type;
		}
	}

	public static class SleepingLocationCheck extends FabricZEvent implements ZSleepingLocationCheck {
		private final LivingEntity entity;
		private final BlockPos pos;

		public SleepingLocationCheck(LivingEntity entity, BlockPos pos) {
			this.entity = entity;
			this.pos = pos;
		}

		@Override
		public LivingEntity getEntity() {
			return entity;
		}

		@Override
		public BlockPos getSleepingLocation() {
			return pos;
		}
	}
}
