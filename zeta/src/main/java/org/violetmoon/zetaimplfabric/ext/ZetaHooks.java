package org.violetmoon.zetaimplfabric.ext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lookups for the mixins that call Zeta's block/item extensions from vanilla code, at the places Forge calls the
 * matching IForgeBlock/IForgeItem methods.
 * <p>
 * An extension is only returned when the block/item class overrides that hook: some Zeta defaults differ from what
 * vanilla does (e.g. canEquipZeta is false), and blocks/items that don't override a hook must keep vanilla behaviour.
 */
public final class ZetaHooks {

	private static final Map<Class<?>, Map<String, Boolean>> OVERRIDES = new ConcurrentHashMap<>();

	private ZetaHooks() { }

	public static @Nullable IZetaItemExtensions item(ItemStack stack, String hook) {
		return !stack.isEmpty() && stack.getItem() instanceof IZetaItemExtensions ext && overrides(ext.getClass(), IZetaItemExtensions.class, hook) ? ext : null;
	}

	public static @Nullable IZetaBlockExtensions block(BlockState state, String hook) {
		return state.getBlock() instanceof IZetaBlockExtensions ext && overrides(ext.getClass(), IZetaBlockExtensions.class, hook) ? ext : null;
	}

	/**
	 * Forge: player.getMainHandItem().doesSneakBypassUse(...) && player.getOffhandItem().doesSneakBypassUse(...), where
	 * an empty stack always bypasses and items bypass only if they say so.
	 */
	public static boolean sneakBypassesUse(Player player, LevelReader level, BlockPos pos) {
		return sneakBypassesUse(player.getMainHandItem(), player, level, pos) && sneakBypassesUse(player.getOffhandItem(), player, level, pos);
	}

	private static boolean sneakBypassesUse(ItemStack stack, Player player, LevelReader level, BlockPos pos) {
		if(stack.isEmpty())
			return true;
		IZetaItemExtensions ext = item(stack, "doesSneakBypassUseZeta");
		return ext != null && ext.doesSneakBypassUseZeta(stack, level, pos, player);
	}

	private static boolean overrides(Class<?> clazz, Class<?> api, String hook) {
		return OVERRIDES.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>()).computeIfAbsent(hook, h -> {
			for(Method m : clazz.getMethods())
				if(m.getName().equals(h) && m.getDeclaringClass() != api)
					return true;
			return false;
		});
	}
}
