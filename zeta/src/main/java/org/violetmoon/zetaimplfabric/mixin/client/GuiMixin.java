package org.violetmoon.zetaimplfabric.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zetaimplfabric.client.event.FabricZClientEvents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Forge renders the HUD as separate overlays (ForgeGui) with Pre/Post events around each. These wrap the matching
 * vanilla Gui calls; ArmorLevel is the armor row inside renderPlayerHealth.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

	@Unique private float zeta$partialTick;

	@Inject(method = "render", at = @At("HEAD"))
	private void zeta$capturePartialTick(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
		zeta$partialTick = partialTick;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V"))
	private void zeta$hotbar(Gui gui, float partialTick, GuiGraphics guiGraphics, Operation<Void> original) {
		FabricZClientEvents.fireOverlay(guiGraphics, partialTick, 39, ZRenderGuiOverlay.Hotbar.Pre.class);
		original.call(gui, partialTick, guiGraphics);
		FabricZClientEvents.fireOverlay(guiGraphics, partialTick, 39, ZRenderGuiOverlay.Hotbar.Post.class);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void zeta$crosshair(Gui gui, GuiGraphics guiGraphics, Operation<Void> original) {
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.Crosshair.Pre.class);
		original.call(gui, guiGraphics);
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.Crosshair.Post.class);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void zeta$playerHealth(Gui gui, GuiGraphics guiGraphics, Operation<Void> original) {
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.PlayerHealth.Pre.class);
		original.call(gui, guiGraphics);
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.PlayerHealth.Post.class);
	}

	@Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = "ldc=armor", shift = At.Shift.AFTER))
	private void zeta$armorPre(GuiGraphics guiGraphics, CallbackInfo ci) {
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, zeta$armorLeftHeight(), ZRenderGuiOverlay.ArmorLevel.Pre.class);
	}

	@Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=health"))
	private void zeta$armorPost(GuiGraphics guiGraphics, CallbackInfo ci) {
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, zeta$armorLeftHeight(), ZRenderGuiOverlay.ArmorLevel.Post.class);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void zeta$potionIcons(Gui gui, GuiGraphics guiGraphics, Operation<Void> original) {
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.PotionIcons.Pre.class);
		original.call(gui, guiGraphics);
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.PotionIcons.Post.class);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void zeta$debugText(DebugScreenOverlay overlay, GuiGraphics guiGraphics, Operation<Void> original) {
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.DebugText.Pre.class);
		original.call(overlay, guiGraphics);
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.DebugText.Post.class);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;III)V"))
	private void zeta$chat(ChatComponent chat, GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, Operation<Void> original) {
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.ChatPanel.Pre.class);
		original.call(chat, guiGraphics, tickCount, mouseX, mouseY);
		FabricZClientEvents.fireOverlay(guiGraphics, zeta$partialTick, 39, ZRenderGuiOverlay.ChatPanel.Post.class);
	}

	/**
	 * Forge's ForgeGui#leftHeight when the armor overlay renders: 39 plus the height of the health rows.
	 */
	private int zeta$armorLeftHeight() {
		if(!(Minecraft.getInstance().getCameraEntity() instanceof Player player))
			return 39;
		float maxHealth = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), Mth.ceil(player.getHealth()));
		int absorb = Mth.ceil(player.getAbsorptionAmount());
		int healthRows = Mth.ceil((maxHealth + absorb) / 2.0F / 10.0F);
		int rowHeight = Math.max(10 - (healthRows - 2), 3);
		return 39 + healthRows * rowHeight + (rowHeight != 10 ? 10 - rowHeight : 0);
	}
}
