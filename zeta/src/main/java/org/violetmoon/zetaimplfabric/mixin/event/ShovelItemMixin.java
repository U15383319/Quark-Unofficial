package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.violetmoon.zeta.util.ToolActions;
import org.violetmoon.zetaimplfabric.event.play.FabricZToolHooks;

import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ShovelItem.class)
public abstract class ShovelItemMixin {

	// the flattened state looked up from FLATTENABLES
	@ModifyVariable(method = "useOn", at = @At("STORE"), ordinal = 1)
	private @Nullable BlockState zeta$flatten(@Nullable BlockState vanilla, UseOnContext context) {
		return FabricZToolHooks.modify(Optional.ofNullable(vanilla), context, ToolActions.SHOVEL_FLATTEN).orElse(null);
	}
}
