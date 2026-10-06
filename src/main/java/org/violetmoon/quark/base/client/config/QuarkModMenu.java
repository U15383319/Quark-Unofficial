package org.violetmoon.quark.base.client.config;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Fabric: Mod Menu replaces Forge's ConfigScreenFactory extension point.
 */
public class QuarkModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return QuarkConfigHomeScreen::new;
	}
}
