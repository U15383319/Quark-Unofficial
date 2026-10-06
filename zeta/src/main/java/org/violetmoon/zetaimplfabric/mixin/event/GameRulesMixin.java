package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.zetaimplfabric.event.play.FabricZEntityEvents;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.level.GameRules;

/**
 * Forge EntityMobGriefingEvent: every read of the mobGriefing rule (see FabricZEntityEvents#fireMobGriefing).
 */
@Mixin(GameRules.class)
public class GameRulesMixin {

	@ModifyReturnValue(method = "getBoolean", at = @At("RETURN"))
	private boolean zeta$mobGriefing(boolean value, GameRules.Key<GameRules.BooleanValue> key) {
		return key == GameRules.RULE_MOBGRIEFING ? FabricZEntityEvents.fireMobGriefing(value) : value;
	}
}
