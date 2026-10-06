package org.violetmoon.quark.content.tools.module;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.item.TrowelItem;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tools")
public class TrowelModule extends ZetaModule {

	@Config(
		name = "Trowel Max Durability",
		description = "Amount of blocks placed is this value + 1.\nSet to 0 to make the Trowel unbreakable"
	)
	@Config.Min(0)
	public static int maxDamage = 0;

	@Hint
	Item trowel;

	@LoadEvent
	public final void register(ZRegister event) {
		trowel = new TrowelItem(this);
	}

	public static final TagKey<Item> blacklist = TagKey.create(Registries.ITEM, Quark.asResource("trowel_blacklist"));
	public static final TagKey<Item> whitelist = TagKey.create(Registries.ITEM, Quark.asResource("trowel_whitelist"));

}
