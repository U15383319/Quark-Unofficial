package org.violetmoon.quark.base;

import org.violetmoon.quark.integration.lootr.client.IClientLootrIntegration;
import org.violetmoon.zeta.client.ClientTicker;
import org.violetmoon.zeta.client.ZetaClient;
import org.violetmoon.zeta.util.ZetaSide;
import org.violetmoon.zetaimplfabric.client.FabricZetaClient;

public class QuarkClient {

	static {
		if (Quark.ZETA.side == ZetaSide.SERVER)
			throw new IllegalAccessError("SOMEONE LOADED QuarkClient ON THE SERVER!!!! DON'T DO THAT!!!!!!");
	}

	public static final ZetaClient ZETA_CLIENT = new FabricZetaClient(Quark.ZETA);
	public static final ClientTicker ticker = ClientTicker.INSTANCE;

	public static final String MISC_GROUP = "quark.gui.keygroup.misc";
	public static final String INV_GROUP = "quark.gui.keygroup.inv";
	public static final String EMOTE_GROUP = "quark.gui.keygroup.emote";

	// Fabric: Lootr support comes with the integration phase
	public static final IClientLootrIntegration LOOTR_INTEGRATION = new IClientLootrIntegration.Dummy();

}
