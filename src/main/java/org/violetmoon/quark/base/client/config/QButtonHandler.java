package org.violetmoon.quark.base.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.event.bus.PlayEvent;

import java.util.List;

public class QButtonHandler {

	@PlayEvent
	public static void onGuiInit(ZScreen.Init.Post event) {
		if(!QuarkGeneralConfig.enableQButton)
			return;

		List<String> targetButtonTranslationKeys = getTargetButtons(event.getScreen());
		if(targetButtonTranslationKeys == null)
			return;

		// the first target button (in priority order) that the screen has
		for(String key : targetButtonTranslationKeys) {
			String name = I18n.get(key);
			for(GuiEventListener listener : event.getListenersList())
				if(listener instanceof AbstractWidget widget && name.equals(widget.getMessage().getString())) {
					int x = widget.getX();

					if(QuarkGeneralConfig.qButtonOnRight)
						x += widget.getWidth() + 4; //4px of padding
					else
						x -= 24; //4px of padding, accounting for the 20px width of the Q button

					Button qButton = new QButton(x, widget.getY());
					event.addListener(qButton);
					return;
				}
		}
	}

	private static @Nullable List<String> getTargetButtons(Screen gui) {
		if(gui instanceof TitleScreen)
			if(QuarkGeneralConfig.qButtonOnRight)
				return List.of("menu.online"); // Minecraft Realms
			else
				// Forge has its own Mods button; on Fabric it comes from Mod Menu, and without Mod Menu the Q button goes next to Realms
				return List.of("modmenu.title", "menu.online");

		if(gui instanceof PauseScreen)
			if(QuarkGeneralConfig.qButtonOnRight)
				return List.of("menu.shareToLan", "menu.playerReporting"); // Open to LAN, Player Reporting
			else
				return List.of("menu.options"); // Options...

		return null;
	}

}
