package org.violetmoon.quark.gametest;

import static org.violetmoon.quark.gametest.QuarkTestUtil.block;
import static org.violetmoon.quark.gametest.QuarkTestUtil.item;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.violetmoon.quark.content.world.block.CorundumBlock;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tweaks and the vanilla behaviour hooks behind them.
 */
public class QuarkTweakTests implements FabricGameTest {

	// Magma Keeps Concrete Powder (ConcretePowderBlockMixin): powder on magma stays powder next to water
	@GameTest(template = EMPTY_STRUCTURE)
	public void magmaKeepsConcretePowder(GameTestHelper helper) {
		BlockPos onMagma = new BlockPos(1, 2, 2);
		BlockPos onStone = new BlockPos(3, 2, 2);
		helper.setBlock(onMagma.below(), Blocks.MAGMA_BLOCK);
		helper.setBlock(onStone.below(), Blocks.STONE);
		helper.setBlock(onMagma, Blocks.WHITE_CONCRETE_POWDER);
		helper.setBlock(onStone, Blocks.WHITE_CONCRETE_POWDER);
		helper.setBlock(new BlockPos(2, 2, 2), Blocks.WATER); // touches both

		helper.succeedWhen(() -> {
			helper.assertBlockPresent(Blocks.WHITE_CONCRETE, onStone);
			helper.assertBlockPresent(Blocks.WHITE_CONCRETE_POWDER, onMagma);
		});
	}

	// Zombie Villagers On Normal (LivingConversionEvent.Pre in Zombie#killedEntity, before vanilla's 50% roll on Normal)
	@GameTest(template = EMPTY_STRUCTURE)
	public void zombieVillagersOnNormal(GameTestHelper helper) {
		var server = helper.getLevel().getServer();
		Difficulty before = server.getWorldData().getDifficulty();
		server.setDifficulty(Difficulty.NORMAL, true);
		try {
			Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
			zombie.setNoAi(true);
			int converted = 0;
			for(int i = 0; i < 8; i++) {
				Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(3, 2, 3));
				villager.setNoAi(true);
				zombie.killedEntity(helper.getLevel(), villager);
				if(villager.isRemoved())
					converted++;
			}
			helper.assertTrue(converted == 8, converted + " of 8 villagers turned into zombie villagers");
			helper.assertTrue(helper.getEntities(EntityType.ZOMBIE_VILLAGER, new BlockPos(3, 2, 3), 3).size() == 8, "zombie villagers missing");
		} finally {
			server.setDifficulty(before, true);
		}
		helper.succeed();
	}

	// Glass Shards: the converted glass loot table (quark:flag condition) drops shards without silk touch
	@GameTest(template = EMPTY_STRUCTURE)
	public void glassDropsShards(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
		List<ItemStack> drops = Block.getDrops(Blocks.GLASS.defaultBlockState(), helper.getLevel(), pos, null);
		int shards = drops.stream().filter(s -> s.is(item("clear_shard"))).mapToInt(ItemStack::getCount).sum();
		helper.assertTrue(shards >= 1 && shards <= 4, "glass dropped " + drops);
		helper.succeed();
	}

	// Simple Harvest: right-clicking a ripe crop harvests and replants it (ZRightClickBlock on Fabric's UseBlockCallback)
	@GameTest(template = EMPTY_STRUCTURE)
	public void simpleHarvestReplantsWheat(GameTestHelper helper) {
		BlockPos crop = new BlockPos(2, 2, 2);
		helper.setBlock(crop.below(), Blocks.FARMLAND);
		helper.setBlock(crop, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE));

		FakePlayer player = FakePlayer.get(helper.getLevel());
		BlockPos abs = helper.absolutePos(crop);
		player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));

		helper.assertBlockPresent(Blocks.WHEAT, crop);
		helper.assertTrue(helper.getBlockState(crop).getValue(CropBlock.AGE) == 0, "wheat wasn't replanted");
		helper.succeed();
	}

	// Slabs To Blocks: two slabs craft back into the block (custom recipe, built from the loaded recipes)
	@GameTest(template = EMPTY_STRUCTURE)
	public void twoSlabsCraftBlock(GameTestHelper helper) {
		TransientCraftingContainer grid = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
			@Override
			public ItemStack quickMoveStack(Player player, int index) {
				return ItemStack.EMPTY;
			}

			@Override
			public boolean stillValid(Player player) {
				return true;
			}
		}, 3, 3);
		grid.setItem(0, new ItemStack(Items.STONE_SLAB));
		grid.setItem(1, new ItemStack(Items.STONE_SLAB));

		Optional<CraftingRecipe> recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "no recipe for two stone slabs");
		ItemStack result = recipe.get().assemble(grid, helper.getLevel().registryAccess());
		helper.assertTrue(result.is(Items.STONE), "two stone slabs craft " + result);
		helper.succeed();
	}

	// getBeaconColorMultiplier: corundum tints the beacon beam (vanilla only tints with stained glass)
	@GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
	public void corundumTintsBeacon(GameTestHelper helper) {
		BlockPos beacon = new BlockPos(1, 2, 1);
		BlockPos control = new BlockPos(5, 2, 1); // vanilla: stained glass
		CorundumBlock corundum = (CorundumBlock) block("red_corundum");
		for(BlockPos center : List.of(beacon, control)) {
			// a one-level pyramid, so that the beacon activates and reports its beam
			for(int x = -1; x <= 1; x++)
				for(int z = -1; z <= 1; z++)
					helper.setBlock(center.offset(x, -1, z), Blocks.IRON_BLOCK);
			helper.setBlock(center, Blocks.BEACON);
		}
		helper.setBlock(beacon.above(), corundum);
		helper.setBlock(control.above(), Blocks.RED_STAINED_GLASS);

		helper.succeedWhen(() -> {
			List<BeaconBlockEntity.BeaconBeamSection> controlSections = ((BeaconBlockEntity) helper.getBlockEntity(control)).getBeamSections();
			helper.assertTrue(controlSections.size() >= 2, "vanilla beam has " + controlSections.size() + " sections");
			List<BeaconBlockEntity.BeaconBeamSection> sections = ((BeaconBlockEntity) helper.getBlockEntity(beacon)).getBeamSections();
			helper.assertTrue(sections.size() >= 2, "beam has " + sections.size() + " sections");
			helper.assertTrue(Arrays.equals(sections.get(1).getColor(), corundum.colorComponents),
				"beam colour " + Arrays.toString(sections.get(1).getColor()) + ", corundum is " + Arrays.toString(corundum.colorComponents));
		});
	}

	// isFlammable: fire can hang in the air next to a paper wall (FireBlock#canSurvive asks canBurn of the neighbours)
	@GameTest(template = EMPTY_STRUCTURE)
	public void paperWallIsFlammable(GameTestHelper helper) {
		BlockPos wall = new BlockPos(1, 2, 2);
		BlockPos stone = new BlockPos(4, 2, 2);
		helper.setBlock(wall, block("paper_wall"));
		helper.setBlock(stone, Blocks.STONE);

		BlockState fire = Blocks.FIRE.defaultBlockState();
		helper.assertTrue(fire.canSurvive(helper.getLevel(), helper.absolutePos(wall.east())), "fire can't burn next to a paper wall");
		helper.assertFalse(fire.canSurvive(helper.getLevel(), helper.absolutePos(stone.east())), "fire can burn next to stone");
		helper.succeed();
	}
}
