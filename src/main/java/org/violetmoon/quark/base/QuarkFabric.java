package org.violetmoon.quark.base;

import org.violetmoon.zetaimplfabric.FabricZeta;
import org.violetmoon.zetaimplfabric.mod.ZetaModFabric;

import net.fabricmc.api.ModInitializer;

/**
 * Quark's Fabric entrypoint. Forge constructed {@link Quark} through @Mod and drove the rest with mod-bus events.
 */
public class QuarkFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		ZetaModFabric.ensureInitialized();

		new Quark();
		((FabricZeta) Quark.ZETA).finishCommonSetup();
	}
}
