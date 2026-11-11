package org.violetmoon.quark.gametest;

import static org.violetmoon.quark.gametest.QuarkTestUtil.block;
import static org.violetmoon.quark.gametest.QuarkTestUtil.item;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tweaks.module.GoldToolsHaveFortuneModule;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Things the Forge side of Zeta did that the Fabric port has to replicate (see PORTING.md). Run these against the
 * released jars too (runProductionGametest): several of them only broke there.
 */
public class QuarkParityTests implements FabricGameTest {

	// Forge WeatheringCopperMixin: an axe scrapes Quark's copper vertical slabs like vanilla copper
	@GameTest(template = EMPTY_STRUCTURE)
	public void axeScrapesCopperVerticalSlab(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, block("exposed_cut_copper_vertical_slab"));
		use(helper, pos, new ItemStack(Items.IRON_AXE));
		helper.assertBlockPresent(block("cut_copper_vertical_slab"), pos);
		helper.succeed();
	}

	// Zeta's PottedPlantRegistry: Quark plants go in flower pots
	@GameTest(template = EMPTY_STRUCTURE)
	public void quarkSaplingGoesInFlowerPot(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, Blocks.FLOWER_POT);
		use(helper, pos, new ItemStack(item("blue_blossom_sapling")));
		helper.assertBlockPresent(block("potted_blue_blossom_sapling"), pos);
		helper.succeed();
	}

	// Gold Tools Have Fortune, through both Zeta's item extensions and vanilla's enchantment lookup
	@GameTest(template = EMPTY_STRUCTURE)
	public void goldToolsHaveFortune(GameTestHelper helper) {
		ItemStack pickaxe = new ItemStack(Items.GOLDEN_PICKAXE);
		int expected = GoldToolsHaveFortuneModule.fortuneLevel;
		int viaZeta = Quark.ZETA.itemExtensions.get(pickaxe).getEnchantmentLevelZeta(pickaxe, Enchantments.BLOCK_FORTUNE);
		int viaVanilla = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, pickaxe);
		boolean listed = Quark.ZETA.itemExtensions.get(pickaxe).getAllEnchantmentsZeta(pickaxe).containsKey(Enchantments.BLOCK_FORTUNE);
		helper.assertTrue(viaZeta == expected, "Zeta extensions say fortune " + viaZeta + ", expected " + expected);
		helper.assertTrue(viaVanilla == expected, "EnchantmentHelper says fortune " + viaVanilla + ", expected " + expected);
		helper.assertTrue(listed, "getAllEnchantmentsZeta doesn't list fortune");
		helper.succeed();
	}

	// IZetaBlockExtensions default (Forge IForgeBlock): enchanting power comes from the enchantment_power_provider tag
	@GameTest(template = EMPTY_STRUCTURE)
	public void vanillaBookshelfHasEnchantPower(GameTestHelper helper) {
		var state = Blocks.BOOKSHELF.defaultBlockState();
		float power = Quark.ZETA.blockExtensions.get(state).getEnchantPowerBonusZeta(state, helper.getLevel(), helper.absolutePos(BlockPos.ZERO));
		helper.assertTrue(power == 1, "bookshelf enchant power " + power);
		helper.succeed();
	}

	// Quark Woven Oddities (Forge: Quark Oddities) turns the oddities category on in production; in dev it is always on
	@GameTest(template = EMPTY_STRUCTURE)
	public void odditiesCategoryEnabled(GameTestHelper helper) {
		java.util.List<String> off = new java.util.ArrayList<>();
		for(var module : Quark.ZETA.modules.getModules())
			if(module.category().name.equals("oddities") && !module.isEnabled() && !module.disabledByOverlap())
				off.add(module.displayName());
		helper.assertTrue(off.isEmpty(), "oddities modules off: " + off);
		helper.succeed();
	}

	private static void use(GameTestHelper helper, BlockPos pos, ItemStack stack) {
		FakePlayer player = FakePlayer.get(helper.getLevel());
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		BlockPos abs = helper.absolutePos(pos);
		player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
	}
}
