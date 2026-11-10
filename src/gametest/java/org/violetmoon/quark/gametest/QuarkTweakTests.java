package org.violetmoon.quark.gametest;

import static org.violetmoon.quark.gametest.QuarkTestUtil.block;
import static org.violetmoon.quark.gametest.QuarkTestUtil.item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FireBlock;
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
	@GameTest(template = EMPTY_STRUCTURE)
	public void corundumTintsBeacon(GameTestHelper helper) {
		BlockPos beacon = new BlockPos(1, 1, 2);
		BlockPos control = new BlockPos(4, 1, 2); // vanilla: stained glass
		CorundumBlock corundum = (CorundumBlock) block("red_corundum");
		helper.setBlock(beacon, Blocks.BEACON);
		helper.setBlock(beacon.above(), corundum);
		helper.setBlock(control, Blocks.BEACON);
		helper.setBlock(control.above(), Blocks.RED_STAINED_GLASS);

		helper.succeedWhen(() -> {
			List<BeaconBlockEntity.BeaconBeamSection> controlSections = beamSections(helper, control);
			helper.assertTrue(controlSections.size() >= 2, "vanilla beam has " + controlSections.size() + " sections");
			List<BeaconBlockEntity.BeaconBeamSection> sections = beamSections(helper, beacon);
			helper.assertTrue(sections.size() >= 2, "beam has " + sections.size() + " sections");
			helper.assertTrue(Arrays.equals(sections.get(1).getColor(), corundum.colorComponents),
				"beam colour " + Arrays.toString(sections.get(1).getColor()) + ", corundum is " + Arrays.toString(corundum.colorComponents));
		});
	}

	// isFlammable / getFireSpreadSpeed: paper walls catch fire like Forge says they do
	@GameTest(template = EMPTY_STRUCTURE)
	public void paperWallIsFlammable(GameTestHelper helper) throws ReflectiveOperationException {
		FireBlock fire = (FireBlock) Blocks.FIRE;
		BlockPos wall = new BlockPos(2, 1, 2);
		helper.setBlock(wall, block("paper_wall"));

		Method canBurn = FireBlock.class.getDeclaredMethod("canBurn", BlockState.class);
		canBurn.setAccessible(true);
		helper.assertTrue((boolean) canBurn.invoke(fire, helper.getBlockState(wall)), "paper wall can't burn");

		Method igniteOdds = FireBlock.class.getDeclaredMethod("getIgniteOdds", LevelReader.class, BlockPos.class);
		igniteOdds.setAccessible(true);
		int odds = (int) igniteOdds.invoke(fire, helper.getLevel(), helper.absolutePos(wall.east()));
		helper.assertTrue(odds == 60, "fire next to a paper wall has ignite odds " + odds);
		helper.succeed();
	}

	// getBeamSections() is empty while the beacon has no pyramid; the computed sections are what the hook changes
	@SuppressWarnings("unchecked")
	private static List<BeaconBlockEntity.BeaconBeamSection> beamSections(GameTestHelper helper, BlockPos pos) {
		try {
			Field field = BeaconBlockEntity.class.getDeclaredField("beamSections");
			field.setAccessible(true);
			return (List<BeaconBlockEntity.BeaconBeamSection>) field.get(helper.getBlockEntity(pos));
		} catch(ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
