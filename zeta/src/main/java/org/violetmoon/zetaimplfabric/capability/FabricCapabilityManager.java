package org.violetmoon.zetaimplfabric.capability;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.capability.ZetaCapability;
import org.violetmoon.zeta.capability.ZetaCapabilityManager;
import org.violetmoon.zeta.event.play.loading.ZAttachCapabilities;
import org.violetmoon.zetaimplfabric.event.FabricZetaEvents;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Forge attaches capabilities when an object is created. Here the attach event is fired the first time an object is
 * queried, and the result is cached per object instance.
 * <p>
 * Singleton for the same reason as on Forge: the attach event goes out on the shared play bus.
 */
public final class FabricCapabilityManager implements ZetaCapabilityManager {

	public static final FabricCapabilityManager INSTANCE = new FabricCapabilityManager();

	// weak keys: BlockEntity and Level don't override equals/hashCode, so this is effectively identity
	private final Map<Object, Map<ZetaCapability<?>, Object>> attached = Collections.synchronizedMap(new WeakHashMap<>());
	// ItemStack capabilities are keyed by Item: every attach handler only looks at the item, and stacks are copied constantly
	private final Map<Item, Map<ZetaCapability<?>, Object>> itemCaps = new ConcurrentHashMap<>();

	private FabricCapabilityManager() { }

	@Override
	public ZetaCapabilityManager register(ZetaCapability<?> cap, Object backing) {
		// nothing platform-specific backs a capability on Fabric
		return this;
	}

	@Override
	public <T> boolean hasCapability(ZetaCapability<T> cap, ItemStack stack) {
		return getCapability(cap, stack) != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> @Nullable T getCapability(ZetaCapability<T> cap, ItemStack stack) {
		if(stack.isEmpty())
			return null;
		Map<ZetaCapability<?>, Object> caps = itemCaps.computeIfAbsent(stack.getItem(), item -> fire(new ItemCaps(stack), ZAttachCapabilities.ItemStackCaps.class));
		return (T) caps.get(cap);
	}

	@Override
	public <T> boolean hasCapability(ZetaCapability<T> cap, BlockEntity be) {
		return getCapability(cap, be) != null;
	}

	@Override
	public <T> @Nullable T getCapability(ZetaCapability<T> cap, BlockEntity be) {
		return lookup(cap, be, BlockEntityCaps::new, ZAttachCapabilities.BlockEntityCaps.class);
	}

	@Override
	public <T> boolean hasCapability(ZetaCapability<T> cap, Level level) {
		return getCapability(cap, level) != null;
	}

	@Override
	public <T> @Nullable T getCapability(ZetaCapability<T> cap, Level level) {
		return lookup(cap, level, LevelCaps::new, ZAttachCapabilities.LevelCaps.class);
	}

	@Override
	public <T> void attachCapability(Object target, ResourceLocation id, ZetaCapability<T> cap, T impl) {
		((Attach<?>) target).addCapability(id, cap, impl);
	}

	/**
	 * Drops cached item capabilities, so attach handlers of modules that were enabled/disabled by a config change apply
	 * to new lookups (on Forge, new stacks would pick them up).
	 */
	public void invalidateItemCaps() {
		itemCaps.clear();
	}

	@SuppressWarnings("unchecked")
	private <O, T, E extends Attach<O>> @Nullable T lookup(ZetaCapability<T> cap, O object, Function<O, E> eventFactory, Class<? super E> firedAs) {
		Map<ZetaCapability<?>, Object> caps = attached.get(object);
		if(caps == null) {
			caps = fire(eventFactory.apply(object), firedAs);
			attached.put(object, caps);
		}
		return (T) caps.get(cap);
	}

	@SuppressWarnings("unchecked")
	private static <E extends Attach<?>> Map<ZetaCapability<?>, Object> fire(E event, Class<? super E> firedAs) {
		FabricZetaEvents.firePlay(event, (Class<Object>) (Class<?>) firedAs);
		return event.caps.isEmpty() ? Map.of() : event.caps;
	}

	private abstract static class Attach<O> implements ZAttachCapabilities<O> {
		private final O object;
		final Map<ZetaCapability<?>, Object> caps = new IdentityHashMap<>();

		Attach(O object) {
			this.object = object;
		}

		@Override
		public ZetaCapabilityManager getCapabilityManager() {
			return INSTANCE;
		}

		@Override
		public O getObject() {
			return object;
		}

		@Override
		public <C> void addCapability(ResourceLocation key, ZetaCapability<C> cap, C impl) {
			caps.put(cap, impl);
		}
	}

	private static final class ItemCaps extends Attach<ItemStack> implements ZAttachCapabilities.ItemStackCaps {
		ItemCaps(ItemStack object) {
			super(object);
		}
	}

	private static final class BlockEntityCaps extends Attach<BlockEntity> implements ZAttachCapabilities.BlockEntityCaps {
		BlockEntityCaps(BlockEntity object) {
			super(object);
		}
	}

	private static final class LevelCaps extends Attach<Level> implements ZAttachCapabilities.LevelCaps {
		LevelCaps(Level object) {
			super(object);
		}
	}
}
