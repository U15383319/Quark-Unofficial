package org.violetmoon.zetaimplfabric.client.event;

import java.util.List;
import java.util.Map;

import org.violetmoon.zeta.client.event.load.ZAddBlockColorHandlers;
import org.violetmoon.zeta.client.event.load.ZAddItemColorHandlers;
import org.violetmoon.zeta.client.event.load.ZAddModelLayers;
import org.violetmoon.zeta.client.event.load.ZModel;
import org.violetmoon.zeta.client.event.play.ZClientTick;
import org.violetmoon.zeta.client.event.play.ZEarlyRender;
import org.violetmoon.zeta.client.event.play.ZFirstClientTick;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;
import org.violetmoon.zeta.client.event.play.ZHighlightBlock;
import org.violetmoon.zeta.client.event.play.ZInput;
import org.violetmoon.zeta.client.event.play.ZInputUpdate;
import org.violetmoon.zeta.client.event.play.ZRenderContainerScreen;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.client.event.play.ZRenderLiving;
import org.violetmoon.zeta.client.event.play.ZRenderPlayer;
import org.violetmoon.zeta.client.event.play.ZRenderTick;
import org.violetmoon.zeta.client.event.play.ZRenderTooltip;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.client.event.play.ZScreenshot;
import org.violetmoon.zeta.event.bus.IZetaPlayEvent;
import org.violetmoon.zeta.event.play.ZItemTooltip;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zetaimplfabric.event.FabricZEvent;
import org.violetmoon.zetaimplfabric.event.FabricZetaEvents;
import org.violetmoon.zetaimplfabric.event.play.FabricZWorldEvents;
import org.violetmoon.zetaimplfabric.mixin.client.AccessorScreen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * Client-side Zeta events: the Fabric API callbacks are registered in {@link #init()}, the rest are fired from client
 * mixins through the static helpers below.
 */
public final class FabricZClientEvents {

	private FabricZClientEvents() { }

	private static boolean firstTickFired = false;

	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(mc -> FabricZetaEvents.firePlay(new ZClientTick.Start() { }, ZClientTick.Start.class));
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			// Forge: ZetaModClientProxy fired ZFirstClientTick from the first ClientTickEvent
			if(!firstTickFired) {
				firstTickFired = true;
				FabricZetaEvents.fireLoadAll(new ZFirstClientTick() { }, ZFirstClientTick.class);
			}
			FabricZetaEvents.firePlay(new ZClientTick.End() { }, ZClientTick.End.class);
		});

		ClientTickEvents.START_WORLD_TICK.register(level -> FabricZWorldEvents.fireLevelTick(level, true));
		ClientTickEvents.END_WORLD_TICK.register(level -> FabricZWorldEvents.fireLevelTick(level, false));

		ScreenEvents.BEFORE_INIT.register((mc, screen, w, h) -> FabricZetaEvents.firePlay(new ScreenInit(screen), ZScreen.Init.Pre.class));
		ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
			FabricZetaEvents.firePlay(new ScreenInit(screen), ZScreen.Init.Post.class);
			ScreenEvents.beforeRender(screen).register((s, gg, mx, my, pt) -> FabricZetaEvents.firePlay(new ScreenRender(s, gg, mx, my), ZScreen.Render.Pre.class));
			ScreenEvents.afterRender(screen).register((s, gg, mx, my, pt) -> FabricZetaEvents.firePlay(new ScreenRender(s, gg, mx, my), ZScreen.Render.Post.class));
		});

		// Forge RenderHighlightEvent.Block
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> {
			if(!FabricZetaEvents.hasPlayListeners(ZHighlightBlock.class))
				return true;
			HighlightBlock event = new HighlightBlock(context.consumers(), context.camera(), context.matrixStack());
			FabricZetaEvents.firePlay(event, ZHighlightBlock.class);
			return !event.isCanceled();
		});

		// Forge ItemTooltipEvent (end of ItemStack#getTooltipLines)
		ItemTooltipCallback.EVENT.register((stack, flag, lines) -> FabricZetaEvents.firePlay(new ItemTooltip(stack, flag, lines, Minecraft.getInstance().player), ZItemTooltip.class));
	}

	/// helpers called from mixins ///

	public static void fireEarlyRender(GuiGraphics guiGraphics) {
		FabricZetaEvents.firePlay((ZEarlyRender) () -> guiGraphics, ZEarlyRender.class);
	}

	public static void fireRenderTick(float partialTicks, boolean start) {
		if(start)
			FabricZetaEvents.firePlay((ZRenderTick.Start) () -> partialTicks, ZRenderTick.Start.class);
		else
			FabricZetaEvents.firePlay((ZRenderTick.End) () -> partialTicks, ZRenderTick.End.class);
	}

	public static void fireKeyInput(int key, int scanCode, int action) {
		FabricZetaEvents.firePlay(new ZInput.Key() {
			@Override
			public int getKey() {
				return key;
			}

			@Override
			public int getScanCode() {
				return scanCode;
			}

			@Override
			public int getAction() {
				return action;
			}
		}, ZInput.Key.class);
	}

	public static void fireMouseInput(int button, int action) {
		FabricZetaEvents.firePlay(new ZInput.MouseButton() {
			@Override
			public int getButton() {
				return button;
			}

			@Override
			public int getAction() {
				return action;
			}
		}, ZInput.MouseButton.class);
	}

	public static void fireInputUpdate(Player player, Input input) {
		FabricZetaEvents.firePlay(new ZInputUpdate() {
			@Override
			public Input getInput() {
				return input;
			}

			@Override
			public Player getEntity() {
				return player;
			}
		}, ZInputUpdate.class);
	}

	public static void fireContainerScreen(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY, boolean foreground) {
		ContainerScreenRender event = new ContainerScreenRender(screen, guiGraphics, mouseX, mouseY);
		if(foreground)
			FabricZetaEvents.firePlay(event, ZRenderContainerScreen.Foreground.class);
		else
			FabricZetaEvents.firePlay(event, ZRenderContainerScreen.Background.class);
	}

	public static void fireOverlay(GuiGraphics guiGraphics, float partialTick, int leftHeight, Class<? extends ZRenderGuiOverlay> type) {
		if(!FabricZetaEvents.hasPlayListeners(type))
			return;
		FabricZetaEvents.firePlayAs(new GuiOverlay(guiGraphics, partialTick, leftHeight), type);
	}

	public static void fireRenderLiving(Entity entity, PoseStack pose, boolean pre) {
		RenderLiving event = new RenderLiving(entity, pose);
		FabricZetaEvents.firePlayAs(event, pre ? ZRenderLiving.PreHighest.class : ZRenderLiving.PostLowest.class);
	}

	public static void fireRenderPlayer(Player player, PlayerRenderer renderer, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, boolean pre) {
		RenderPlayer event = new RenderPlayer(player, renderer, partialTick, pose, buffers, light);
		FabricZetaEvents.firePlayAs(event, pre ? ZRenderPlayer.Pre.class : ZRenderPlayer.Post.class);
	}

	/**
	 * Forge RenderTooltipEvent.GatherComponents: ZGatherTooltipComponents and ZRenderTooltip.GatherComponents wrap the
	 * same Forge event, Low listeners run last.
	 *
	 * @return the (possibly changed) max width
	 */
	public static int fireGatherTooltip(ItemStack stack, int screenWidth, int screenHeight, List<Either<FormattedText, TooltipComponent>> elements, int maxWidth) {
		GatherTooltip event = new GatherTooltip(stack, screenWidth, screenHeight, elements, maxWidth);
		FabricZetaEvents.firePlay(event, ZGatherTooltipComponents.class);
		FabricZetaEvents.firePlay(event, ZRenderTooltip.GatherComponents.class);
		FabricZetaEvents.firePlay(event, ZRenderTooltip.GatherComponents.Low.class);
		return event.maxWidth;
	}

	public static void fireScreenshot() {
		FabricZetaEvents.firePlay(new ZScreenshot() { }, ZScreenshot.class);
	}

	public static boolean fireMouseClickPre(Screen screen, double x, double y, int button) {
		ScreenMouseButton event = new ScreenMouseButton(screen, x, y, button);
		FabricZetaEvents.firePlay(event, ZScreen.MouseButtonPressed.Pre.class);
		return event.isCanceled();
	}

	public static boolean fireMouseClickPost(Screen screen, double x, double y, int button) {
		ScreenMouseButton event = new ScreenMouseButton(screen, x, y, button);
		FabricZetaEvents.firePlay(event, ZScreen.MouseButtonPressed.Post.class);
		return event.isCanceled();
	}

	public static boolean fireMouseScrollPre(Screen screen, double delta) {
		ScreenMouseScroll event = new ScreenMouseScroll(screen, delta);
		FabricZetaEvents.firePlay(event, ZScreen.MouseScrolled.Pre.class);
		return event.isCanceled();
	}

	public static boolean fireMouseScrollPost(Screen screen, double delta) {
		ScreenMouseScroll event = new ScreenMouseScroll(screen, delta);
		FabricZetaEvents.firePlay(event, ZScreen.MouseScrolled.Post.class);
		return event.isCanceled();
	}

	public static boolean fireKeyPressed(Screen screen, int keyCode, int scanCode, int modifiers, boolean pre) {
		ScreenKey event = new ScreenKey(screen, keyCode, scanCode, modifiers);
		FabricZetaEvents.firePlayAs(event, pre ? ZScreen.KeyPressed.Pre.class : ZScreen.KeyPressed.Post.class);
		return event.isCanceled();
	}

	public static boolean fireCharTyped(Screen screen, char codePoint, int modifiers, boolean pre) {
		ScreenChar event = new ScreenChar(screen, codePoint, modifiers);
		FabricZetaEvents.firePlayAs(event, pre ? ZScreen.CharacterTyped.Pre.class : ZScreen.CharacterTyped.Post.class);
		return event.isCanceled();
	}

	/**
	 * @return the event; check isCanceled and getNewScreen
	 */
	public static ScreenOpening fireScreenOpening(Screen current, Screen newScreen) {
		ScreenOpening event = new ScreenOpening(current, newScreen);
		FabricZetaEvents.firePlay(event, ZScreen.Opening.class);
		return event;
	}

	public static void fireBlockColors(BlockColors colors) {
		FabricZetaEvents.fireLoadAll(new ZAddBlockColorHandlers() {
			@Override
			public void register(BlockColor c, Block... blocks) {
				colors.register(c, blocks);
			}

			@Override
			public void registerNamed(Zeta myZeta, java.util.function.Function<Block, BlockColor> c, String... names) {
				for(String name : names)
					myZeta.registry.assignBlockColor(name, b -> register(c.apply(b), b));
			}

			@Override
			public BlockColors getBlockColors() {
				return colors;
			}
		}, ZAddBlockColorHandlers.class);
	}

	public static void fireItemColors(ItemColors colors, BlockColors blockColors) {
		FabricZetaEvents.fireLoadAll(new ZAddItemColorHandlers() {
			@Override
			public void register(ItemColor c, ItemLike... items) {
				colors.register(c, items);
			}

			@Override
			public void registerNamed(Zeta myZeta, java.util.function.Function<Item, ItemColor> c, String... names) {
				for(String name : names)
					myZeta.registry.assignItemColor(name, i -> register(c.apply(i), i));
			}

			@Override
			public ItemColors getItemColors() {
				return colors;
			}
		}, ZAddItemColorHandlers.class);
	}

	public static void fireAddModelLayers(Map<EntityType<?>, EntityRenderer<?>> renderers, EntityModelSet models) {
		FabricZetaEvents.fireLoadAll(new ZAddModelLayers() {
			@SuppressWarnings("unchecked")
			@Override
			public <T extends LivingEntity, R extends LivingEntityRenderer<T, ? extends EntityModel<T>>> R getRenderer(EntityType<? extends T> entityType) {
				return (R) renderers.get(entityType);
			}

			@Override
			public EntityModelSet getEntityModels() {
				return models;
			}
		}, ZAddModelLayers.class);
	}

	public static void fireModifyBakingResult(Map<ResourceLocation, BakedModel> models, ModelBakery bakery) {
		FabricZetaEvents.fireLoadAll(new ZModel.ModifyBakingResult() {
			@Override
			public Map<ResourceLocation, BakedModel> getModels() {
				return models;
			}

			@Override
			public ModelBakery getModelBakery() {
				return bakery;
			}
		}, ZModel.ModifyBakingResult.class);
	}

	/// event classes ///

	public record ItemTooltip(ItemStack getItemStack, TooltipFlag getFlags, List<Component> getToolTip, Player getEntity) implements ZItemTooltip { }

	public static class ScreenInit implements ZScreen.Init.Pre, ZScreen.Init.Post {
		private final Screen screen;

		public ScreenInit(Screen screen) {
			this.screen = screen;
		}

		@Override
		public Screen getScreen() {
			return screen;
		}

		@Override
		public List<GuiEventListener> getListenersList() {
			return ((AccessorScreen) screen).zeta$getChildren();
		}

		@Override
		public void addListener(GuiEventListener listener) {
			// Forge ScreenEvent.Init#addListener: children, plus renderables/narratables when applicable
			AccessorScreen acc = (AccessorScreen) screen;
			if(listener instanceof Renderable r)
				acc.zeta$getRenderables().add(r);
			if(listener instanceof NarratableEntry n)
				acc.zeta$getNarratables().add(n);
			acc.zeta$getChildren().add(listener);
		}

		@Override
		public void removeListener(GuiEventListener listener) {
			((AccessorScreen) screen).zeta$removeWidget(listener);
		}
	}

	public record ScreenRender(Screen getScreen, GuiGraphics getGuiGraphics, int getMouseX, int getMouseY) implements ZScreen.Render.Pre, ZScreen.Render.Post { }

	public static class ScreenMouseButton extends FabricZEvent implements ZScreen.MouseButtonPressed.Pre, ZScreen.MouseButtonPressed.Post {
		private final Screen screen;
		private final double x, y;
		private final int button;

		public ScreenMouseButton(Screen screen, double x, double y, int button) {
			this.screen = screen;
			this.x = x;
			this.y = y;
			this.button = button;
		}

		@Override
		public Screen getScreen() {
			return screen;
		}

		@Override
		public int getButton() {
			return button;
		}

		@Override
		public double getMouseX() {
			return x;
		}

		@Override
		public double getMouseY() {
			return y;
		}
	}

	public static class ScreenMouseScroll extends FabricZEvent implements ZScreen.MouseScrolled.Pre, ZScreen.MouseScrolled.Post {
		private final Screen screen;
		private final double delta;

		public ScreenMouseScroll(Screen screen, double delta) {
			this.screen = screen;
			this.delta = delta;
		}

		@Override
		public Screen getScreen() {
			return screen;
		}

		@Override
		public double getScrollDelta() {
			return delta;
		}
	}

	public static class ScreenKey extends FabricZEvent implements ZScreen.KeyPressed.Pre, ZScreen.KeyPressed.Post {
		private final Screen screen;
		private final int keyCode, scanCode, modifiers;

		public ScreenKey(Screen screen, int keyCode, int scanCode, int modifiers) {
			this.screen = screen;
			this.keyCode = keyCode;
			this.scanCode = scanCode;
			this.modifiers = modifiers;
		}

		@Override
		public Screen getScreen() {
			return screen;
		}

		@Override
		public int getKeyCode() {
			return keyCode;
		}

		@Override
		public int getScanCode() {
			return scanCode;
		}

		@Override
		public int getModifiers() {
			return modifiers;
		}
	}

	public static class ScreenChar extends FabricZEvent implements ZScreen.CharacterTyped.Pre, ZScreen.CharacterTyped.Post {
		private final Screen screen;
		private final char codePoint;
		private final int modifiers;

		public ScreenChar(Screen screen, char codePoint, int modifiers) {
			this.screen = screen;
			this.codePoint = codePoint;
			this.modifiers = modifiers;
		}

		@Override
		public Screen getScreen() {
			return screen;
		}

		@Override
		public char getCodePoint() {
			return codePoint;
		}

		@Override
		public int getModifiers() {
			return modifiers;
		}
	}

	public static class ScreenOpening extends FabricZEvent implements ZScreen.Opening {
		private final Screen current;
		private Screen newScreen;

		public ScreenOpening(Screen current, Screen newScreen) {
			this.current = current;
			this.newScreen = newScreen;
		}

		@Override
		public Screen getScreen() {
			return newScreen;
		}

		@Override
		public Screen getCurrentScreen() {
			return current;
		}

		@Override
		public Screen getNewScreen() {
			return newScreen;
		}

		@Override
		public void setNewScreen(Screen newScreen) {
			this.newScreen = newScreen;
		}
	}

	public static class HighlightBlock extends FabricZEvent implements ZHighlightBlock {
		private final MultiBufferSource buffers;
		private final Camera camera;
		private final PoseStack pose;

		public HighlightBlock(MultiBufferSource buffers, Camera camera, PoseStack pose) {
			this.buffers = buffers;
			this.camera = camera;
			this.pose = pose;
		}

		@Override
		public MultiBufferSource getMultiBufferSource() {
			return buffers;
		}

		@Override
		public Camera getCamera() {
			return camera;
		}

		@Override
		public PoseStack getPoseStack() {
			return pose;
		}
	}

	public record ContainerScreenRender(AbstractContainerScreen<?> getContainerScreen, GuiGraphics getGuiGraphics, int getMouseX, int getMouseY)
		implements ZRenderContainerScreen.Foreground, ZRenderContainerScreen.Background { }

	/**
	 * One class for every overlay; the type it is fired as decides which listeners run.
	 */
	public static class GuiOverlay implements ZRenderGuiOverlay.Hotbar.Pre, ZRenderGuiOverlay.Hotbar.Post,
		ZRenderGuiOverlay.Crosshair.Pre, ZRenderGuiOverlay.Crosshair.Post,
		ZRenderGuiOverlay.PlayerHealth.Pre, ZRenderGuiOverlay.PlayerHealth.Post,
		ZRenderGuiOverlay.ArmorLevel.Pre, ZRenderGuiOverlay.ArmorLevel.Post,
		ZRenderGuiOverlay.DebugText.Pre, ZRenderGuiOverlay.DebugText.Post,
		ZRenderGuiOverlay.PotionIcons.Pre, ZRenderGuiOverlay.PotionIcons.Post,
		ZRenderGuiOverlay.ChatPanel.Pre, ZRenderGuiOverlay.ChatPanel.Post {

		private final GuiGraphics guiGraphics;
		private final float partialTick;
		private final int leftHeight;

		public GuiOverlay(GuiGraphics guiGraphics, float partialTick, int leftHeight) {
			this.guiGraphics = guiGraphics;
			this.partialTick = partialTick;
			this.leftHeight = leftHeight;
		}

		@Override
		public Window getWindow() {
			return Minecraft.getInstance().getWindow();
		}

		@Override
		public GuiGraphics getGuiGraphics() {
			return guiGraphics;
		}

		@Override
		public float getPartialTick() {
			return partialTick;
		}

		@Override
		public boolean shouldDrawSurvivalElements() {
			Minecraft mc = Minecraft.getInstance();
			return mc.gameMode != null && mc.gameMode.canHurtPlayer() && mc.getCameraEntity() instanceof Player;
		}

		@Override
		public int getLeftHeight() {
			return leftHeight;
		}
	}

	public record RenderLiving(Entity getEntity, PoseStack getPoseStack) implements ZRenderLiving.PreHighest, ZRenderLiving.PostLowest { }

	public record RenderPlayer(Player getEntity, PlayerRenderer getRenderer, float getPartialTick, PoseStack getPoseStack,
							   MultiBufferSource getMultiBufferSource, int getPackedLight) implements ZRenderPlayer.Pre, ZRenderPlayer.Post { }

	public static class GatherTooltip implements ZGatherTooltipComponents, ZRenderTooltip.GatherComponents.Low {
		private final ItemStack stack;
		private final int screenWidth, screenHeight;
		private final List<Either<FormattedText, TooltipComponent>> elements;
		private int maxWidth;

		public GatherTooltip(ItemStack stack, int screenWidth, int screenHeight, List<Either<FormattedText, TooltipComponent>> elements, int maxWidth) {
			this.stack = stack;
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
			this.elements = elements;
			this.maxWidth = maxWidth;
		}

		@Override
		public ItemStack getItemStack() {
			return stack;
		}

		@Override
		public int getScreenWidth() {
			return screenWidth;
		}

		@Override
		public int getScreenHeight() {
			return screenHeight;
		}

		@Override
		public List<Either<FormattedText, TooltipComponent>> getTooltipElements() {
			return elements;
		}

		@Override
		public int getMaxWidth() {
			return maxWidth;
		}

		@Override
		public void setMaxWidth(int maxWidth) {
			this.maxWidth = maxWidth;
		}
	}
}
