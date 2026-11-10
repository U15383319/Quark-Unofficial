package org.violetmoon.quark.gametest;

import static org.violetmoon.quark.gametest.QuarkTestUtil.block;
import static org.violetmoon.quark.gametest.QuarkTestUtil.count;

import org.violetmoon.zeta.item.handler.IItemHandler;
import org.violetmoon.zeta.item.handler.ItemHandlers;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Automation blocks: item transfer into Quark blocks (Fabric Transfer API), pistons, magnets, dispensers.
 */
public class QuarkAutomationTests implements FabricGameTest {

	// a vanilla hopper feeds the chute (ItemHandlers -> Fabric ItemStorage), which drops the items below
	@GameTest(template = EMPTY_STRUCTURE)
	public void hopperFeedsChute(GameTestHelper helper) {
		BlockPos chute = new BlockPos(2, 2, 2);
		helper.setBlock(chute, block("chute"));
		helper.setBlock(chute.above(), Blocks.HOPPER);
		((Container) helper.getBlockEntity(chute.above())).setItem(0, new ItemStack(Items.DIAMOND, 3));

		helper.succeedWhen(() -> helper.assertItemEntityCountIs(Items.DIAMOND, chute.below(), 1.5, 3));
	}

	// a vanilla hopper fills the crate through the crate's item handler
	@GameTest(template = EMPTY_STRUCTURE)
	public void hopperFillsCrate(GameTestHelper helper) {
		BlockPos crate = new BlockPos(2, 1, 2);
		helper.setBlock(crate, block("crate"));
		helper.setBlock(crate.above(), Blocks.HOPPER);
		((Container) helper.getBlockEntity(crate.above())).setItem(0, new ItemStack(Items.EMERALD, 5));

		helper.succeedWhen(() -> {
			IItemHandler handler = ItemHandlers.get(helper.getBlockEntity(crate), Direction.UP).orElseThrow();
			int emeralds = 0;
			for(int i = 0; i < handler.getSlots(); i++)
				if(handler.getStackInSlot(i).is(Items.EMERALD))
					emeralds += handler.getStackInSlot(i).getCount();
			helper.assertTrue(emeralds == 5, "crate holds " + emeralds + " emeralds");
		});
	}

	// hopper -> pipe -> chest
	@GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
	public void pipeCarriesItemsToChest(GameTestHelper helper) {
		BlockPos chest = new BlockPos(2, 1, 2);
		BlockPos pipe = chest.above();
		BlockPos hopper = pipe.above();
		helper.setBlock(chest, Blocks.CHEST);
		helper.setBlock(hopper, Blocks.HOPPER);
		helper.setBlock(pipe, block("pipe"));
		((Container) helper.getBlockEntity(hopper)).setItem(0, new ItemStack(Items.GOLD_INGOT, 2));

		helper.succeedWhen(() -> {
			int gold = count((Container) helper.getBlockEntity(chest), Items.GOLD_INGOT);
			helper.assertTrue(gold == 2, "chest holds " + gold + " gold ingots");
		});
	}

	// Pistons Move Tile Entities: vanilla pistons can't push chests at all
	@GameTest(template = EMPTY_STRUCTURE)
	public void pistonPushesChestWithContents(GameTestHelper helper) {
		BlockPos piston = new BlockPos(1, 1, 2);
		BlockPos chest = piston.east();
		helper.setBlock(piston, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.EAST));
		helper.setBlock(chest, Blocks.CHEST);
		((Container) helper.getBlockEntity(chest)).setItem(0, new ItemStack(Items.DIAMOND, 7));
		helper.setBlock(piston.west(), Blocks.REDSTONE_BLOCK);

		helper.succeedWhen(() -> {
			helper.assertBlockPresent(Blocks.CHEST, chest.east());
			BlockEntity moved = helper.getBlockEntity(chest.east());
			helper.assertTrue(moved instanceof Container c && count(c, Items.DIAMOND) == 7, "pushed chest lost its items");
		});
	}

	// an iron rod pushed by a piston breaks the block in front of it
	@GameTest(template = EMPTY_STRUCTURE)
	public void ironRodBreaksBlock(GameTestHelper helper) {
		BlockPos piston = new BlockPos(1, 1, 2);
		BlockPos rod = piston.east();
		BlockPos target = rod.east();
		helper.setBlock(piston, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.EAST));
		helper.setBlock(rod, block("iron_rod").defaultBlockState().setValue(DirectionalBlock.FACING, Direction.EAST));
		helper.setBlock(target, Blocks.COBBLESTONE);
		helper.setBlock(piston.west(), Blocks.REDSTONE_BLOCK);

		helper.succeedWhen(() -> {
			helper.assertBlockPresent(block("iron_rod"), target);
			helper.assertBlockPresent(Blocks.PISTON_HEAD, rod);
		});
	}

	// Dispensers Place Blocks: a vanilla dispenser would throw the item out instead
	@GameTest(template = EMPTY_STRUCTURE)
	public void dispenserPlacesBlock(GameTestHelper helper) {
		BlockPos dispenser = new BlockPos(1, 1, 2);
		helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
		((Container) helper.getBlockEntity(dispenser)).setItem(0, new ItemStack(Items.OAK_PLANKS));
		helper.setBlock(dispenser.west(), Blocks.REDSTONE_BLOCK);

		helper.succeedWhenBlockPresent(Blocks.OAK_PLANKS, dispenser.east());
	}

	// a powered magnet pushes the iron block in front of it away
	@GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
	public void magnetMovesIronBlock(GameTestHelper helper) {
		BlockPos magnet = new BlockPos(1, 1, 2);
		BlockPos iron = magnet.east(2);
		helper.setBlock(magnet, block("magnet").defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST));
		helper.setBlock(iron, Blocks.IRON_BLOCK);
		helper.setBlock(magnet.west(), Blocks.REDSTONE_BLOCK);

		helper.succeedWhen(() -> {
			helper.assertBlockNotPresent(Blocks.IRON_BLOCK, iron);
			helper.assertBlockPresent(Blocks.IRON_BLOCK, iron.east());
		});
	}
}
