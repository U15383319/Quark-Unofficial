package org.violetmoon.zetaimplfabric.event.play;

import java.util.Optional;

import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zeta.util.ToolAction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forge BlockState#getToolModifiedState for the vanilla tools.
 */
public final class FabricZToolHooks {

	private FabricZToolHooks() { }

	/**
	 * @param vanilla what the vanilla tool would turn the clicked block into
	 * @return the final state after asking the block (Zeta blocks) and firing BlockToolModification
	 */
	public static Optional<BlockState> modify(Optional<BlockState> vanilla, UseOnContext context, ToolAction action) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);

		BlockState proposed = state.getBlock() instanceof IZetaBlockExtensions ext
			? ext.getToolModifiedStateZeta(state, context, action.name(), false)
			: vanilla.orElse(null);

		return Optional.ofNullable(FabricZWorldEvents.fireToolModification(level, pos, state, action, proposed));
	}
}
