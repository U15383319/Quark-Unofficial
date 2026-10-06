package org.violetmoon.zetaimplfabric.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public interface AccessorScreen {

	@Accessor("children")
	List<GuiEventListener> zeta$getChildren();

	@Accessor("renderables")
	List<Renderable> zeta$getRenderables();

	@Accessor("narratables")
	List<NarratableEntry> zeta$getNarratables();

	@Invoker("removeWidget")
	void zeta$removeWidget(GuiEventListener listener);
}
