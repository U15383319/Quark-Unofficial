package org.violetmoon.quark.mixin.mixins.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.violetmoon.quark.base.item.QuarkMusicDiscItem;
import org.violetmoon.quark.content.tools.module.AmbientDiscsModule;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	@Inject(
		method = "playStreamingMusic",
		at = @At(value = "JUMP", ordinal = 1),
		cancellable = true
	)
	public void playStreamingMusic(SoundEvent soundIn, BlockPos pos, CallbackInfo info) {
		RecordItem musicDiscItem = soundIn == null ? null : RecordItem.getBySound(soundIn); // Fabric: vanilla has no disc parameter
		if(musicDiscItem instanceof QuarkMusicDiscItem quarkDisc && AmbientDiscsModule.Client.playAmbientSound(quarkDisc, pos))
			info.cancel();
	}

}
