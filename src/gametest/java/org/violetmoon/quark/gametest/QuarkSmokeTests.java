package org.violetmoon.quark.gametest;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.phys.Vec3;

/**
 * Every Quark block and entity: placed/spawned, saved and loaded, and every block has its loot table.
 */
public class QuarkSmokeTests implements FabricGameTest {

	@GameTest(template = EMPTY_STRUCTURE)
	public void everyBlockHasItsLootTable(GameTestHelper helper) {
		var loot = helper.getLevel().getServer().getLootData();
		List<String> missing = new ArrayList<>();
		for(Block block : quarkBlocks()) {
			ResourceLocation table = block.getLootTable();
			if(!table.equals(BuiltInLootTables.EMPTY) && loot.getElementOptional(LootDataType.TABLE, table).isEmpty())
				missing.add(BuiltInRegistries.BLOCK.getKey(block) + " (" + table + ")");
		}
		helper.assertTrue(missing.isEmpty(), "missing loot tables: " + missing);
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE)
	public void everyBlockEntityPlacesAndReloads(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		List<String> failures = new ArrayList<>();
		int checked = 0;
		for(Block block : quarkBlocks()) {
			// placeholders like the magnet's moving block (and vanilla's moving piston) only get a block entity while moving
			if(!(block instanceof EntityBlock entityBlock) || entityBlock.newBlockEntity(BlockPos.ZERO, block.defaultBlockState()) == null)
				continue;
			String id = BuiltInRegistries.BLOCK.getKey(block).toString();
			try {
				helper.setBlock(pos, block);
				BlockEntity be = helper.getBlockEntity(pos);
				if(be == null) {
					failures.add(id + ": no block entity");
					continue;
				}
				CompoundTag tag = be.saveWithFullMetadata();
				if(BlockEntity.loadStatic(be.getBlockPos(), be.getBlockState(), tag) == null)
					failures.add(id + ": block entity doesn't load");
				checked++;
			} catch(Exception e) {
				failures.add(id + ": " + e);
			} finally {
				helper.setBlock(pos, Blocks.AIR);
			}
		}
		helper.assertTrue(failures.isEmpty(), "block entities: " + failures);
		helper.assertTrue(checked > 10, "only " + checked + " block entities checked");
		helper.succeed();
	}

	@GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
	public void everyEntitySpawnsTicksAndReloads(GameTestHelper helper) {
		List<String> failures = new ArrayList<>();
		List<Entity> spawned = new ArrayList<>();
		Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 1, 3)));

		for(EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
			if(!id.getNamespace().equals("quark"))
				continue;
			try {
				Entity entity = type.create(helper.getLevel());
				if(entity == null) {
					failures.add(id + ": can't be created");
					continue;
				}
				entity.moveTo(at.x, at.y, at.z, 0, 0);
				if(entity instanceof Mob mob)
					mob.setNoAi(true);
				helper.getLevel().addFreshEntity(entity);
				spawned.add(entity);
			} catch(Exception e) {
				failures.add(id + ": spawn " + e);
			}
		}

		helper.runAfterDelay(20, () -> {
			for(Entity entity : spawned) {
				ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
				try {
					CompoundTag tag = new CompoundTag();
					if(entity.isAlive() && entity.save(tag) && EntityType.create(tag, helper.getLevel()).isEmpty())
						failures.add(id + ": doesn't load");
				} catch(Exception e) {
					failures.add(id + ": save/load " + e);
				}
				entity.discard();
			}
			helper.assertTrue(failures.isEmpty(), "entities: " + failures);
			helper.assertTrue(spawned.size() > 10, "only " + spawned.size() + " entities spawned");
			helper.succeed();
		});
	}

	private static List<Block> quarkBlocks() {
		List<Block> blocks = new ArrayList<>();
		for(Block block : BuiltInRegistries.BLOCK)
			if(BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("quark"))
				blocks.add(block);
		return blocks;
	}
}
