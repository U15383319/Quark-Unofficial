package org.violetmoon.quark.base;

import org.violetmoon.zetaimplfabric.client.FabricZetaClient;
import org.violetmoon.zetaimplfabric.mod.ZetaModFabricClient;

import net.fabricmc.api.ClientModInitializer;

/**
 * Quark's Fabric client entrypoint: fires the client setup events Forge sent on the mod bus.
 */
public class QuarkFabricClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ZetaModFabricClient.ensureInitialized();

		((FabricZetaClient) QuarkClient.ZETA_CLIENT).finishClientSetup();
	}
}
