package org.violetmoon.quark.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

//entity that has additional behavior when moved by magnets
public interface IMagneticEntity {

    /**
     * Here you can react to magnets moving your entity. If you implement this interface it is YOUR responsibility to ACTUALLY move the entity.
     * You can override it for conditional movement or extra move actions.
     */
    default void moveByMagnet(Entity self, Vec3 moveDirection, BlockEntity tile){
        self.push(moveDirection.x(), moveDirection.y(), moveDirection.z());
    }

}
