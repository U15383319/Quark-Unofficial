package org.violetmoon.zetaimplfabric.util;

import org.violetmoon.zeta.util.RaytracingUtil;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FabricRaytracingUtil extends RaytracingUtil {
	@Override
	public double getEntityRange(LivingEntity player) {
		// Forge reads the BLOCK_REACH attribute; Fabric has no reach attribute, so use the vanilla values
		return player instanceof Player p && p.getAbilities().instabuild ? 5.0 : 4.5;
	}
}
