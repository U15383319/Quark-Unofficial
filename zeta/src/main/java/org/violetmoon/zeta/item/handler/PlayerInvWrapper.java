package org.violetmoon.zeta.item.handler;

import net.minecraft.world.entity.player.Inventory;

/**
 * Fabric stand-in for Forge's net.minecraftforge.items.wrapper.PlayerInvWrapper: the whole player inventory
 * (main, armor, offhand) with Inventory's slot numbering.
 */
public class PlayerInvWrapper extends InvWrapper {
	public PlayerInvWrapper(Inventory inv) {
		super(inv);
	}
}
