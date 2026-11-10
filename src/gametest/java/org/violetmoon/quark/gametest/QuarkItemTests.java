package org.violetmoon.quark.gametest;

import static org.violetmoon.quark.gametest.QuarkTestUtil.item;

import org.violetmoon.quark.content.tools.module.TrowelModule;
import org.violetmoon.zeta.item.handler.ItemHandlers;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

/**
 * Item extension hooks (Forge IForgeItem) on Quark items.
 */
public class QuarkItemTests implements FabricGameTest {

	// onEntityItemUpdate: a dropped backpack spills its contents
	@GameTest(template = EMPTY_STRUCTURE)
	public void droppedBackpackSpillsContents(GameTestHelper helper) {
		ItemStack backpack = new ItemStack(item("backpack"));
		ItemStack rest = ItemHandlers.get(backpack).orElseThrow().insertItem(0, new ItemStack(Items.DIAMOND, 3), false);
		helper.assertTrue(rest.isEmpty(), "backpack didn't take the diamonds");

		BlockPos pos = new BlockPos(2, 2, 2);
		Vec3 at = Vec3.atCenterOf(helper.absolutePos(pos));
		helper.getLevel().addFreshEntity(new ItemEntity(helper.getLevel(), at.x, at.y, at.z, backpack));

		helper.succeedWhen(() -> helper.assertItemEntityCountIs(Items.DIAMOND, pos, 3, 3));
	}

	// getEnchantmentValue: backpacks can't be enchanted at a table (the leather armor they are made of can)
	@GameTest(template = EMPTY_STRUCTURE)
	public void backpackHasNoEnchantmentValue(GameTestHelper helper) {
		RandomSource random = RandomSource.create(0);
		helper.assertTrue(EnchantmentHelper.getEnchantmentCost(random, 2, 15, new ItemStack(item("backpack"))) == 0, "backpack can be enchanted at a table");
		helper.assertTrue(EnchantmentHelper.getEnchantmentCost(random, 2, 15, new ItemStack(Items.LEATHER_CHESTPLATE)) > 0, "leather chestplate can't be enchanted at a table");
		helper.succeed();
	}

	// isBookEnchantable: the anvil won't apply books to a backpack
	@GameTest(template = EMPTY_STRUCTURE)
	public void anvilRefusesBooksOnBackpack(GameTestHelper helper) {
		ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.ALL_DAMAGE_PROTECTION, 1));
		helper.assertTrue(anvilResult(helper, new ItemStack(item("backpack")), book.copy()).isEmpty(), "anvil applies a book to a backpack");
		helper.assertFalse(anvilResult(helper, new ItemStack(Items.LEATHER_CHESTPLATE), book.copy()).isEmpty(), "anvil doesn't apply a book to a leather chestplate");
		helper.succeed();
	}

	// getMaxDamage(stack) / isDamageable: the trowel's durability comes from its config, not the item properties
	@GameTest(template = EMPTY_STRUCTURE)
	public void trowelDurabilityFollowsConfig(GameTestHelper helper) {
		ItemStack trowel = new ItemStack(item("trowel"));
		helper.assertTrue(trowel.getMaxDamage() == TrowelModule.maxDamage, "trowel max damage " + trowel.getMaxDamage() + ", config " + TrowelModule.maxDamage);
		helper.assertTrue(trowel.isDamageableItem() == (TrowelModule.maxDamage > 0), "trowel damageable: " + trowel.isDamageableItem());
		helper.succeed();
	}

	private static ItemStack anvilResult(GameTestHelper helper, ItemStack left, ItemStack right) {
		FakePlayer player = FakePlayer.get(helper.getLevel());
		AnvilMenu menu = new AnvilMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(BlockPos.ZERO)));
		menu.getSlot(0).set(left);
		menu.getSlot(1).set(right);
		return menu.getSlot(2).getItem();
	}
}
