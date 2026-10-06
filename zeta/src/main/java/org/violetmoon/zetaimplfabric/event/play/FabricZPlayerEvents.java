package org.violetmoon.zetaimplfabric.event.play;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.event.bus.ZResult;
import org.violetmoon.zeta.event.play.entity.ZEntityInteract;
import org.violetmoon.zeta.event.play.entity.ZEntityItemPickup;
import org.violetmoon.zeta.event.play.entity.player.ZPlayer;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerDestroyItem;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerInteract;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickItem;
import org.violetmoon.zetaimplfabric.event.FabricZEvent;
import org.violetmoon.zetaimplfabric.event.FabricZetaEvents;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Player-related Zeta play events and the Fabric API callbacks that fire them.
 */
public final class FabricZPlayerEvents {

	private FabricZPlayerEvents() { }

	public static void init() {
		// Forge PlayerInteractEvent.RightClickBlock (MultiPlayerGameMode#useItemOn / ServerPlayerGameMode#useItemOn)
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			RightClickBlock event = fireRightClickBlock(player, hand, hit);
			return event.isCanceled() ? canceledResult(event.getCancellationResult()) : InteractionResult.PASS;
		});

		// Forge PlayerInteractEvent.RightClickItem
		UseItemCallback.EVENT.register((player, level, hand) -> {
			RightClickItem event = new RightClickItem(player, hand);
			FabricZetaEvents.firePlay(event, ZPlayerInteract.RightClickItem.class);
			if(!event.isCanceled())
				FabricZetaEvents.firePlay(event, ZRightClickItem.class);
			ItemStack stack = player.getItemInHand(hand);
			return event.isCanceled() ? new InteractionResultHolder<>(canceledResult(event.getCancellationResult()), stack) : InteractionResultHolder.pass(stack);
		});

		// Forge PlayerInteractEvent.EntityInteractSpecific (hit != null) and EntityInteract (hit == null)
		UseEntityCallback.EVENT.register((player, level, hand, target, hit) -> {
			if(hit != null) {
				EntityInteractSpecific event = new EntityInteractSpecific(player, hand, target, hit);
				FabricZetaEvents.firePlay(event, ZPlayerInteract.EntityInteractSpecific.class);
				return event.isCanceled() ? canceledResult(event.getCancellationResult()) : InteractionResult.PASS;
			}

			EntityInteract event = new EntityInteract(player, hand, target);
			FabricZetaEvents.firePlay(event, ZPlayerInteract.EntityInteract.class);
			if(!event.isCanceled())
				FabricZetaEvents.firePlay(event, ZEntityInteract.class);
			return event.isCanceled() ? canceledResult(event.getCancellationResult()) : InteractionResult.PASS;
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			FabricZetaEvents.firePlay(new LoggedIn(handler.player), ZPlayer.LoggedIn.class));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
			FabricZetaEvents.firePlay(new LoggedOut(handler.player), ZPlayer.LoggedOut.class));
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
			FabricZetaEvents.firePlay(new Clone(newPlayer, oldPlayer), ZPlayer.Clone.class));
	}

	/**
	 * Forge returns the cancellation result even when it is PASS, which stops vanilla. On Fabric PASS means "continue".
	 */
	private static InteractionResult canceledResult(InteractionResult result) {
		return result == InteractionResult.PASS ? InteractionResult.FAIL : result;
	}

	public static RightClickBlock fireRightClickBlock(Player player, InteractionHand hand, BlockHitResult hit) {
		RightClickBlock event = new RightClickBlock(player, hand, hit);
		FabricZetaEvents.firePlay(event, ZPlayerInteract.RightClickBlock.class);
		if(!event.isCanceled())
			FabricZetaEvents.firePlay(event, ZRightClickBlock.class);
		// the Low listeners check isCanceled themselves (Forge receiveCanceled semantics)
		FabricZetaEvents.firePlay(event, ZRightClickBlock.Low.class);
		return event;
	}

	public static void fireTick(Player player, boolean start) {
		if(start)
			FabricZetaEvents.firePlay((ZPlayerTick.Start) () -> player, ZPlayerTick.Start.class);
		else
			FabricZetaEvents.firePlay((ZPlayerTick.End) () -> player, ZPlayerTick.End.class);
	}

	public static float fireBreakSpeed(Player player, BlockState state, float speed) {
		BreakSpeed event = new BreakSpeed(player, state, speed);
		FabricZetaEvents.firePlay(event, ZPlayer.BreakSpeed.class);
		return event.newSpeed;
	}

	public static void fireDestroyItem(Player player, ItemStack original, InteractionHand hand) {
		FabricZetaEvents.firePlay(new ZPlayerDestroyItem() {
			@Override
			public Player getEntity() {
				return player;
			}

			@Override
			public ItemStack getOriginal() {
				return original;
			}

			@Override
			public InteractionHand getHand() {
				return hand;
			}
		}, ZPlayerDestroyItem.class);
	}

	public static void fireItemPickup(Player player, ItemEntity item) {
		FabricZetaEvents.firePlay(new ZEntityItemPickup() {
			@Override
			public ItemEntity getItem() {
				return item;
			}

			@Override
			public Player getEntity() {
				return player;
			}
		}, ZEntityItemPickup.class);
	}

	/// event classes ///

	public static class RightClickBlock extends FabricZEvent implements ZRightClickBlock.Low, ZPlayerInteract.RightClickBlock {
		private final Player player;
		private final InteractionHand hand;
		private final BlockHitResult hit;

		public RightClickBlock(Player player, InteractionHand hand, BlockHitResult hit) {
			this.player = player;
			this.hand = hand;
			this.hit = hit;
		}

		@Override
		public Player getEntity() {
			return player;
		}

		@Override
		public Level getLevel() {
			return player.level();
		}

		@Override
		public BlockPos getPos() {
			return hit.getBlockPos();
		}

		@Override
		public InteractionHand getHand() {
			return hand;
		}

		@Override
		public ItemStack getItemStack() {
			return player.getItemInHand(hand);
		}

		@Override
		public BlockHitResult getHitVec() {
			return hit;
		}

		@Override
		public Direction getFace() {
			return hit.getDirection();
		}

		@Override
		public ZResult getUseBlock() {
			return ZResult.DEFAULT;
		}
	}

	public static class RightClickItem extends FabricZEvent implements ZRightClickItem, ZPlayerInteract.RightClickItem {
		private final Player player;
		private final InteractionHand hand;

		public RightClickItem(Player player, InteractionHand hand) {
			this.player = player;
			this.hand = hand;
		}

		@Override
		public Player getEntity() {
			return player;
		}

		@Override
		public ItemStack getItemStack() {
			return player.getItemInHand(hand);
		}

		@Override
		public InteractionHand getHand() {
			return hand;
		}

		@Override
		public BlockPos getPos() {
			return player.blockPosition();
		}

		@Override
		public Level getLevel() {
			return player.level();
		}
	}

	public static class EntityInteract extends FabricZEvent implements ZEntityInteract, ZPlayerInteract.EntityInteract {
		private final Player player;
		private final InteractionHand hand;
		private final Entity target;

		public EntityInteract(Player player, InteractionHand hand, Entity target) {
			this.player = player;
			this.hand = hand;
			this.target = target;
		}

		@Override
		public Entity getTarget() {
			return target;
		}

		@Override
		public Player getEntity() {
			return player;
		}

		@Override
		public Level getLevel() {
			return player.level();
		}

		@Override
		public InteractionHand getHand() {
			return hand;
		}

		@Override
		public ItemStack getItemStack() {
			return player.getItemInHand(hand);
		}

		@Override
		public BlockPos getPos() {
			return target.blockPosition();
		}
	}

	public static class EntityInteractSpecific extends FabricZEvent implements ZPlayerInteract.EntityInteractSpecific {
		private final Player player;
		private final InteractionHand hand;
		private final Entity target;
		private final @Nullable EntityHitResult hit;

		public EntityInteractSpecific(Player player, InteractionHand hand, Entity target, @Nullable EntityHitResult hit) {
			this.player = player;
			this.hand = hand;
			this.target = target;
			this.hit = hit;
		}

		@Override
		public Entity getTarget() {
			return target;
		}

		@Override
		public Player getEntity() {
			return player;
		}

		@Override
		public InteractionHand getHand() {
			return hand;
		}

		@Override
		public BlockPos getPos() {
			return target.blockPosition();
		}

		@Override
		public Level getLevel() {
			return player.level();
		}

		public @Nullable EntityHitResult getHit() {
			return hit;
		}
	}

	public record LoggedIn(Player getEntity) implements ZPlayer.LoggedIn { }

	public record LoggedOut(Player getEntity) implements ZPlayer.LoggedOut { }

	public record Clone(Player getEntity, Player getOriginal) implements ZPlayer.Clone { }

	public static class BreakSpeed implements ZPlayer.BreakSpeed {
		private final Player player;
		private final BlockState state;
		private final float originalSpeed;
		private float newSpeed;

		public BreakSpeed(Player player, BlockState state, float originalSpeed) {
			this.player = player;
			this.state = state;
			this.originalSpeed = originalSpeed;
			this.newSpeed = originalSpeed;
		}

		@Override
		public Player getEntity() {
			return player;
		}

		@Override
		public BlockState getState() {
			return state;
		}

		@Override
		public float getOriginalSpeed() {
			return originalSpeed;
		}

		@Override
		public void setNewSpeed(float newSpeed) {
			this.newSpeed = newSpeed;
		}
	}
}
