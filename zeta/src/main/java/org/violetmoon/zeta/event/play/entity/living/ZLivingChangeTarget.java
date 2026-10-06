package org.violetmoon.zeta.event.play.entity.living;

import org.violetmoon.zeta.event.bus.Cancellable;
import org.violetmoon.zeta.event.bus.IZetaPlayEvent;
import org.violetmoon.zeta.event.bus.helpers.LivingGetter;

import net.minecraft.world.entity.LivingEntity;

public interface ZLivingChangeTarget extends IZetaPlayEvent, Cancellable, LivingGetter {
    LivingEntity getNewTarget();
    LivingTargetType getTargetType();

    /**
     * Fabric: mirrors Forge's LivingChangeTargetEvent.LivingTargetType.
     */
    enum LivingTargetType {
        /** Fired from Mob#setTarget. */
        MOB_TARGET,
        /** Fired when a Brain sets ATTACK_TARGET (StartAttacking behavior). */
        BEHAVIOR_TARGET
    }
}
