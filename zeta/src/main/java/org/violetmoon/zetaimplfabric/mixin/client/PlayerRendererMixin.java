package org.violetmoon.zetaimplfabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

	// Forge RenderPlayerEvent.Pre/Post
	@Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
	private void zeta$pre(AbstractClientPlayer player, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light, CallbackInfo ci) {
		FabricZClientEvents.fireRenderPlayer(player, (PlayerRenderer) (Object) this, partialTicks, pose, buffer, light, true);
	}

	@Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
	private void zeta$post(AbstractClientPlayer player, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int light, CallbackInfo ci) {
		FabricZClientEvents.fireRenderPlayer(player, (PlayerRenderer) (Object) this, partialTicks, pose, buffer, light, false);
	}
}
