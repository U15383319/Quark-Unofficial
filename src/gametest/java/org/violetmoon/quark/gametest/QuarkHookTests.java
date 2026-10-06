package org.violetmoon.quark.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Checks the vanilla call sites of Zeta's block extensions (Forge IForgeBlock hooks) in a real level.
 */
public class QuarkHookTests implements FabricGameTest {

	// getEnchantPowerBonus: Quark bookshelves aren't in minecraft:enchantment_power_provider
	@GameTest(template = EMPTY_STRUCTURE)
	public void bookshelfPowersEnchantingTable(GameTestHelper helper) {
		BlockPos table = new BlockPos(3, 1, 3);
		helper.setBlock(table, Blocks.ENCHANTING_TABLE);
		helper.setBlock(table.offset(-2, 0, 0), quark("spruce_bookshelf"));
		helper.assertTrue(EnchantmentTableBlock.isValidBookShelf(helper.getLevel(), helper.absolutePos(table), new BlockPos(-2, 0, 0)), "spruce bookshelf gives no enchanting power");
		helper.succeed();
	}

	// canSustainPlant: saplings grow on hedges
	@GameTest(template = EMPTY_STRUCTURE)
	public void saplingSurvivesOnHedge(GameTestHelper helper) {
		BlockPos soil = new BlockPos(3, 1, 3);
		helper.setBlock(soil, quark("oak_hedge"));
		BlockState sapling = Blocks.OAK_SAPLING.defaultBlockState();
		helper.assertTrue(sapling.canSurvive(helper.getLevel(), helper.absolutePos(soil.above())), "sapling can't stay on a hedge");

		helper.setBlock(soil, Blocks.STONE);
		helper.assertFalse(sapling.canSurvive(helper.getLevel(), helper.absolutePos(soil.above())), "sapling can stay on stone");
		helper.succeed();
	}

	// isLadder: rope is climbable
	@GameTest(template = EMPTY_STRUCTURE)
	public void ropeIsLadder(GameTestHelper helper) {
		BlockPos pos = new BlockPos(3, 2, 3);
		helper.setBlock(pos.above(), Blocks.STONE);
		helper.setBlock(pos, quark("rope"));
		Zombie zombie = helper.spawn(EntityType.ZOMBIE, pos);
		helper.assertTrue(zombie.onClimbable(), "zombie in rope isn't climbing");
		helper.succeed();
	}

	private static Block quark(String name) {
		return BuiltInRegistries.BLOCK.get(new ResourceLocation("quark", name));
	}
}
