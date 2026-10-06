package org.violetmoon.zetaimplfabric.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin {

	// Forge NoteBlockEvent.Play
	@Inject(method = "triggerEvent", at = @At("HEAD"), cancellable = true)
	private void zeta$playNote(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir) {
		if(!FabricZWorldEvents.fireNoteBlock(level, pos, state, state.getValue(NoteBlock.NOTE), state.getValue(NoteBlock.INSTRUMENT)))
			cir.setReturnValue(false);
	}
}
