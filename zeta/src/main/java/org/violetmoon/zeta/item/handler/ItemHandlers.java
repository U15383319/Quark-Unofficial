package org.violetmoon.zeta.item.handler;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Replaces Forge's {@code getCapability(ForgeCapabilities.ITEM_HANDLER, side)}.
 */
public final class ItemHandlers {

	private ItemHandlers() { }

	/**
	 * Item handler of a block entity: its own (IItemHandlerProvider), a wrapper around a vanilla Container
	 * (what Forge attaches to every BaseContainerBlockEntity), or a view of another mod's Fabric item storage.
	 */
	public static Optional<IItemHandler> get(@Nullable BlockEntity be, @Nullable Direction side) {
		if(be == null)
			return Optional.empty();

		if(be instanceof IItemHandlerProvider provider)
			return Optional.ofNullable(provider.getItemHandler(side));

		if(be instanceof ChestBlockEntity && be.getLevel() != null && be.getBlockState().getBlock() instanceof ChestBlock chest) {
			Container container = ChestBlock.getContainer(chest, be.getBlockState(), be.getLevel(), be.getBlockPos(), true);
			if(container != null)
				return Optional.of(new InvWrapper(container));
		}

		if(be instanceof WorldlyContainer worldly && side != null)
			return Optional.of(new SidedInvWrapper(worldly, side));
		if(be instanceof Container container)
			return Optional.of(new InvWrapper(container));

		if(be.getLevel() != null) {
			Storage<ItemVariant> storage = ItemStorage.SIDED.find(be.getLevel(), be.getBlockPos(), be.getBlockState(), be, side);
			if(storage != null)
				return Optional.of(new StorageItemHandler(storage));
		}

		return Optional.empty();
	}

	public static Optional<IItemHandler> get(@Nullable BlockEntity be) {
		return get(be, null);
	}

	/**
	 * Item handler of an item stack (Forge: initCapabilities), for items implementing IItemStackHandlerProvider.
	 */
	public static Optional<IItemHandler> get(ItemStack stack) {
		if(stack.getItem() instanceof IItemStackHandlerProvider provider)
			return Optional.ofNullable(provider.getItemHandler(stack));
		return Optional.empty();
	}

	/**
	 * Exposes the IItemHandlerProvider of these block entities to Fabric's ItemStorage.SIDED (Forge: capabilities were
	 * visible to every mod automatically).
	 */
	@SafeVarargs
	public static <T extends BlockEntity & IItemHandlerProvider> void registerStorage(BlockEntityType<? extends T>... types) {
		ItemStorage.SIDED.registerForBlockEntities((be, side) -> {
			IItemHandler handler = ((IItemHandlerProvider) be).getItemHandler(side);
			return handler == null ? null : new ItemHandlerStorage(handler);
		}, types);
	}
}
