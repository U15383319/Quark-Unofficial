package org.violetmoon.zetaimplfabric.mixin.event;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.violetmoon.zeta.util.ToolActions;
import org.violetmoon.zetaimplfabric.event.play.FabricZToolHooks;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge: AxeItem#useOn asks BlockState#getToolModifiedState for each axe action, which fires
 * BlockToolModificationEvent and lets the block (IZetaBlockExtensions here) pick the result.
 */
@Mixin(AxeItem.class)
public abstract class AxeItemMixin {

	@ModifyVariable(method = "useOn", at = @At("STORE"), ordinal = 0)
	private Optional<BlockState> zeta$strip(Optional<BlockState> vanilla, UseOnContext context) {
		return FabricZToolHooks.modify(vanilla, context, ToolActions.AXE_STRIP);
	}

	@ModifyVariable(method = "useOn", at = @At("STORE"), ordinal = 1)
	private Optional<BlockState> zeta$scrape(Optional<BlockState> vanilla, UseOnContext context) {
		return FabricZToolHooks.modify(vanilla, context, ToolActions.AXE_SCRAPE);
	}

	@ModifyVariable(method = "useOn", at = @At("STORE"), ordinal = 2)
	private Optional<BlockState> zeta$waxOff(Optional<BlockState> vanilla, UseOnContext context) {
		return FabricZToolHooks.modify(vanilla, context, ToolActions.AXE_WAX_OFF);
	}
}
