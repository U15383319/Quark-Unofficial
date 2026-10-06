package org.violetmoon.zetaimplfabric.menu;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Replaces Forge's NetworkHooks#openScreen. Quark's menu types are Fabric ExtendedScreenHandlerTypes (Forge:
 * IForgeMenuType), whose client factory reads the extra data written here.
 */
public final class ZetaMenus {

	private ZetaMenus() { }

	public static void openScreen(ServerPlayer player, MenuProvider provider) {
		openScreen(player, provider, buf -> { });
	}

	public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
		openScreen(player, provider, buf -> buf.writeBlockPos(pos));
	}

	public static void openScreen(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
		player.openMenu(new ExtendedScreenHandlerFactory() {
			@Override
			public void writeScreenOpeningData(ServerPlayer p, FriendlyByteBuf buf) {
				extraData.accept(buf);
			}

			@Override
			public Component getDisplayName() {
				return provider.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
				return provider.createMenu(containerId, inventory, p);
			}
		});
	}
}
