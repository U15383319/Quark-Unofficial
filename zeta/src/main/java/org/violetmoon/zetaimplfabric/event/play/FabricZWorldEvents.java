package org.violetmoon.zetaimplfabric.event.play;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.event.load.ZAddReloadListener;
import org.violetmoon.zeta.event.load.ZTagsUpdated;
import org.violetmoon.zeta.event.play.ZAnvilRepair;
import org.violetmoon.zeta.event.play.ZAnvilUpdate;
import org.violetmoon.zeta.event.play.ZBlock;
import org.violetmoon.zeta.event.play.ZBonemeal;
import org.violetmoon.zeta.event.play.ZFurnaceFuelBurnTime;
import org.violetmoon.zeta.event.play.ZLevelTick;
import org.violetmoon.zeta.event.play.ZPlayNoteBlock;
import org.violetmoon.zeta.event.play.ZServerTick;
import org.violetmoon.zeta.event.play.loading.ZLootTableLoad;
import org.violetmoon.zeta.event.play.loading.ZVillagerTrades;
import org.violetmoon.zeta.event.play.loading.ZWandererTrades;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zeta.util.ToolAction;
import org.violetmoon.zetaimplfabric.event.FabricZEvent;
import org.violetmoon.zetaimplfabric.event.FabricZetaEvents;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Block, level, server and data-loading Zeta play events.
 */
public final class FabricZWorldEvents {

	private FabricZWorldEvents() { }

	private static @Nullable Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> baseTrades;
	private static @Nullable Int2ObjectMap<VillagerTrades.ItemListing[]> baseWandererTrades;

	public static void init() {
		ServerTickEvents.START_SERVER_TICK.register(server -> FabricZetaEvents.firePlay((ZServerTick.Start) () -> server, ZServerTick.Start.class));
		ServerTickEvents.END_SERVER_TICK.register(server -> FabricZetaEvents.firePlay((ZServerTick.End) () -> server, ZServerTick.End.class));

		ServerTickEvents.START_WORLD_TICK.register(level -> fireLevelTick(level, true));
		ServerTickEvents.END_WORLD_TICK.register(level -> fireLevelTick(level, false));

		// Forge BlockEvent.BreakEvent (ServerPlayerGameMode#destroyBlock, before the block is removed)
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			FabricZetaEvents.firePlay(new BlockBreak(level, pos, state, player), ZBlock.Break.class);
			return true;
		});

		// Forge TagsUpdatedEvent
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> FabricZetaEvents.firePlay(new ZTagsUpdated() {
			@Override
			public RegistryAccess getRegistryAccess() {
				return registries;
			}

			@Override
			public boolean isOnClient() {
				return client;
			}
		}, ZTagsUpdated.class));

		// Forge LootTableLoadEvent
		LootTableEvents.REPLACE.register((resourceManager, lootManager, id, original, source) -> {
			LootTableLoad event = new LootTableLoad(id, original);
			FabricZetaEvents.firePlay(event, ZLootTableLoad.class);
			if(event.isCanceled())
				return LootTable.EMPTY;
			return event.table != original ? event.table : null;
		});

		// Forge VillagerTradesEvent / WandererTradesEvent are fired on every server start, from the original trade lists
		ServerLifecycleEvents.SERVER_STARTING.register(server -> rebuildTrades());
	}

	public static void fireLevelTick(Level level, boolean start) {
		if(start)
			FabricZetaEvents.firePlay((ZLevelTick.Start) () -> level, ZLevelTick.Start.class);
		else
			FabricZetaEvents.firePlay((ZLevelTick.End) () -> level, ZLevelTick.End.class);
	}

	public static void fireEntityPlace(LevelAccessor level, BlockPos pos, BlockState placed) {
		FabricZetaEvents.firePlay(new EntityPlace(level, pos, placed), ZBlock.EntityPlace.class);
	}

	/**
	 * Forge BlockEvent.BlockToolModificationEvent.
	 *
	 * @param proposed the state vanilla would produce, or null
	 * @return the final state, or null if the tool does nothing
	 */
	public static @Nullable BlockState fireToolModification(LevelAccessor level, BlockPos pos, BlockState state, ToolAction action, @Nullable BlockState proposed) {
		ToolModification event = new ToolModification(level, pos, state, action, proposed);
		FabricZetaEvents.firePlay(event, ZBlock.BlockToolModification.class);
		return event.finalState;
	}

	/**
	 * @return the event, so the caller can apply its result
	 */
	public static Bonemeal fireBonemeal(Level level, BlockPos pos, BlockState state, ItemStack stack) {
		Bonemeal event = new Bonemeal(level, pos, state, stack);
		FabricZetaEvents.firePlay(event, ZBonemeal.class);
		return event;
	}

	/**
	 * Forge ForgeHooks#getBurnTime: the item's own burn time (Zeta items) or the vanilla/Fabric fuel map, then the event.
	 */
	public static int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
		if(stack.isEmpty())
			return 0;

		int base = stack.getItem() instanceof IZetaItemExtensions ext
			? ext.getBurnTimeZeta(stack, recipeType)
			: AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
		return fireFuelBurnTime(stack, base);
	}

	public static int fireFuelBurnTime(ItemStack stack, int burnTime) {
		if(stack.isEmpty() || !FabricZetaEvents.hasPlayListeners(ZFurnaceFuelBurnTime.class))
			return burnTime;

		FuelBurnTime event = new FuelBurnTime(stack, burnTime);
		FabricZetaEvents.firePlay(event, ZFurnaceFuelBurnTime.class);
		return event.burnTime;
	}

	/**
	 * @return true if the note should play
	 */
	public static boolean fireNoteBlock(LevelAccessor level, BlockPos pos, BlockState state, int note, NoteBlockInstrument instrument) {
		PlayNoteBlock event = new PlayNoteBlock(level, pos, state, note, instrument);
		FabricZetaEvents.firePlay(event, ZPlayNoteBlock.class);
		return !event.isCanceled();
	}

	/**
	 * Forge AnvilUpdateEvent; fired for the Highest, normal and Lowest listeners in order.
	 */
	public static AnvilUpdate fireAnvilUpdate(ItemStack left, ItemStack right, String name, int baseCost, Player player) {
		AnvilUpdate event = new AnvilUpdate(left, right, name, baseCost, player);
		FabricZetaEvents.firePlay(event, ZAnvilUpdate.Highest.class);
		FabricZetaEvents.firePlay(event, ZAnvilUpdate.class);
		FabricZetaEvents.firePlay(event, ZAnvilUpdate.Lowest.class);
		return event;
	}

	/**
	 * Forge AnvilRepairEvent.
	 *
	 * @return the chance the anvil gets damaged
	 */
	public static float fireAnvilRepair(Player player, ItemStack left, ItemStack right, ItemStack output) {
		AnvilRepair event = new AnvilRepair(player, left, right, output);
		FabricZetaEvents.firePlay(event, ZAnvilRepair.class);
		return event.breakChance;
	}

	/**
	 * Forge AddReloadListenerEvent.
	 */
	public static List<PreparableReloadListener> fireAddReloadListener(ReloadableServerResources resources, RegistryAccess access) {
		List<PreparableReloadListener> added = new ArrayList<>();
		FabricZetaEvents.firePlay(new ZAddReloadListener() {
			@Override
			public ReloadableServerResources getServerResources() {
				return resources;
			}

			@Override
			public RegistryAccess getRegistryAccess() {
				return access;
			}

			@Override
			public void addListener(PreparableReloadListener listener) {
				added.add(listener);
			}
		}, ZAddReloadListener.class);
		return added;
	}

	private static void rebuildTrades() {
		if(baseTrades == null) {
			baseTrades = new HashMap<>();
			VillagerTrades.TRADES.forEach((prof, levels) -> baseTrades.put(prof, new Int2ObjectOpenHashMap<>(levels)));
			baseWandererTrades = new Int2ObjectOpenHashMap<>(VillagerTrades.WANDERING_TRADER_TRADES);
		}

		for(VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {
			Int2ObjectMap<VillagerTrades.ItemListing[]> base = baseTrades.getOrDefault(prof, new Int2ObjectOpenHashMap<>());
			Int2ObjectMap<List<VillagerTrades.ItemListing>> mutable = new Int2ObjectOpenHashMap<>();
			for(int i = 1; i <= 5; i++)
				mutable.put(i, new ArrayList<>(Arrays.asList(base.getOrDefault(i, new VillagerTrades.ItemListing[0]))));

			FabricZetaEvents.firePlay(new ZVillagerTrades() {
				@Override
				public Int2ObjectMap<List<VillagerTrades.ItemListing>> getTrades() {
					return mutable;
				}

				@Override
				public VillagerProfession getType() {
					return prof;
				}
			}, ZVillagerTrades.class);

			Int2ObjectMap<VillagerTrades.ItemListing[]> result = new Int2ObjectOpenHashMap<>();
			mutable.int2ObjectEntrySet().forEach(e -> result.put(e.getIntKey(), e.getValue().toArray(new VillagerTrades.ItemListing[0])));
			VillagerTrades.TRADES.put(prof, result);
		}

		List<VillagerTrades.ItemListing> generic = new ArrayList<>(Arrays.asList(baseWandererTrades.getOrDefault(1, new VillagerTrades.ItemListing[0])));
		List<VillagerTrades.ItemListing> rare = new ArrayList<>(Arrays.asList(baseWandererTrades.getOrDefault(2, new VillagerTrades.ItemListing[0])));
		FabricZetaEvents.firePlay(new ZWandererTrades() {
			@Override
			public List<VillagerTrades.ItemListing> getGenericTrades() {
				return generic;
			}

			@Override
			public List<VillagerTrades.ItemListing> getRareTrades() {
				return rare;
			}
		}, ZWandererTrades.class);
		VillagerTrades.WANDERING_TRADER_TRADES.put(1, generic.toArray(new VillagerTrades.ItemListing[0]));
		VillagerTrades.WANDERING_TRADER_TRADES.put(2, rare.toArray(new VillagerTrades.ItemListing[0]));
	}

	/// event classes ///

	public record BlockBreak(LevelAccessor getLevel, BlockPos getPos, BlockState getState, Player getPlayer) implements ZBlock.Break { }

	public record EntityPlace(LevelAccessor getLevel, BlockPos getPos, BlockState getPlacedBlock) implements ZBlock.EntityPlace {
		@Override
		public BlockState getState() {
			return getPlacedBlock;
		}
	}

	public static class ToolModification implements ZBlock.BlockToolModification {
		private final LevelAccessor level;
		private final BlockPos pos;
		private final BlockState state;
		private final ToolAction action;
		private @Nullable BlockState finalState;

		public ToolModification(LevelAccessor level, BlockPos pos, BlockState state, ToolAction action, @Nullable BlockState proposed) {
			this.level = level;
			this.pos = pos;
			this.state = state;
			this.action = action;
			this.finalState = proposed;
		}

		@Override
		public LevelAccessor getLevel() {
			return level;
		}

		@Override
		public BlockPos getPos() {
			return pos;
		}

		@Override
		public BlockState getState() {
			return state;
		}

		@Override
		public ToolAction getToolAction() {
			return action;
		}

		@Override
		public void setFinalState(@Nullable BlockState finalState) {
			this.finalState = finalState;
		}
	}

	public static class Bonemeal extends FabricZEvent implements ZBonemeal {
		private final Level level;
		private final BlockPos pos;
		private final BlockState state;
		private final ItemStack stack;

		public Bonemeal(Level level, BlockPos pos, BlockState state, ItemStack stack) {
			this.level = level;
			this.pos = pos;
			this.state = state;
			this.stack = stack;
		}

		@Override
		public Level getLevel() {
			return level;
		}

		@Override
		public BlockPos getPos() {
			return pos;
		}

		@Override
		public BlockState getBlock() {
			return state;
		}

		@Override
		public ItemStack getStack() {
			return stack;
		}
	}

	public static class FuelBurnTime implements ZFurnaceFuelBurnTime {
		private final ItemStack stack;
		private int burnTime;

		public FuelBurnTime(ItemStack stack, int burnTime) {
			this.stack = stack;
			this.burnTime = burnTime;
		}

		@Override
		public ItemStack getItemStack() {
			return stack;
		}

		@Override
		public void setBurnTime(int time) {
			this.burnTime = time;
		}
	}

	public static class PlayNoteBlock extends FabricZEvent implements ZPlayNoteBlock {
		private final LevelAccessor level;
		private final BlockPos pos;
		private final BlockState state;
		private final int note;
		private final NoteBlockInstrument instrument;

		public PlayNoteBlock(LevelAccessor level, BlockPos pos, BlockState state, int note, NoteBlockInstrument instrument) {
			this.level = level;
			this.pos = pos;
			this.state = state;
			this.note = note;
			this.instrument = instrument;
		}

		@Override
		public LevelAccessor getLevel() {
			return level;
		}

		@Override
		public BlockPos getPos() {
			return pos;
		}

		@Override
		public BlockState getState() {
			return state;
		}

		@Override
		public int getVanillaNoteId() {
			return note;
		}

		@Override
		public NoteBlockInstrument getInstrument() {
			return instrument;
		}
	}

	public static class AnvilUpdate extends FabricZEvent implements ZAnvilUpdate.Highest, ZAnvilUpdate.Lowest {
		private final ItemStack left;
		private final ItemStack right;
		private final String name;
		private final Player player;
		private ItemStack output = ItemStack.EMPTY;
		private int cost;
		private int materialCost = 0;

		public AnvilUpdate(ItemStack left, ItemStack right, String name, int baseCost, Player player) {
			this.left = left;
			this.right = right;
			this.name = name;
			this.cost = baseCost;
			this.player = player;
		}

		@Override
		public ItemStack getLeft() {
			return left;
		}

		@Override
		public ItemStack getRight() {
			return right;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ItemStack getOutput() {
			return output;
		}

		@Override
		public void setOutput(ItemStack output) {
			this.output = output;
		}

		public int getCost() {
			return cost;
		}

		@Override
		public void setCost(int cost) {
			this.cost = cost;
		}

		@Override
		public int getMaterialCost() {
			return materialCost;
		}

		@Override
		public void setMaterialCost(int materialCost) {
			this.materialCost = materialCost;
		}

		@Override
		public Player getPlayer() {
			return player;
		}
	}

	public static class AnvilRepair implements ZAnvilRepair {
		private final Player player;
		private final ItemStack left;
		private final ItemStack right;
		private final ItemStack output;
		private float breakChance = 0.12F;

		public AnvilRepair(Player player, ItemStack left, ItemStack right, ItemStack output) {
			this.player = player;
			this.left = left;
			this.right = right;
			this.output = output;
		}

		@Override
		public Player getEntity() {
			return player;
		}

		@Override
		public ItemStack getOutput() {
			return output;
		}

		@Override
		public ItemStack getLeft() {
			return left;
		}

		@Override
		public ItemStack getRight() {
			return right;
		}

		@Override
		public float getBreakChance() {
			return breakChance;
		}

		@Override
		public void setBreakChance(float breakChance) {
			this.breakChance = breakChance;
		}
	}

	public static class LootTableLoad extends FabricZEvent implements ZLootTableLoad {
		private final ResourceLocation name;
		private LootTable table;

		public LootTableLoad(ResourceLocation name, LootTable table) {
			this.name = name;
			this.table = table;
		}

		@Override
		public ResourceLocation getName() {
			return name;
		}

		@Override
		public LootTable getTable() {
			return table;
		}

		@Override
		public void setTable(LootTable table) {
			this.table = table;
		}
	}
}
