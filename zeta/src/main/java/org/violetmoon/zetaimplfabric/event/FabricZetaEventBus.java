package org.violetmoon.zetaimplfabric.event;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.event.bus.Cancellable;
import org.violetmoon.zeta.event.bus.ZetaEventBus;

/**
 * Fabric event bus for Zeta events.
 * <p>
 * Dispatch is exact-type, like the Forge implementation: a listener taking {@code ZFoo.Bar} only receives events fired
 * as {@code ZFoo.Bar}. Events fired without an explicit type are dispatched as each Zeta event interface their class
 * directly implements (Forge did this through its remapper).
 * <p>
 * Listener lists are copy-on-write, because modules (un)subscribe when the config is reloaded while other threads may
 * be firing events.
 */
public class FabricZetaEventBus<E> extends ZetaEventBus<E> {

	private final Map<Class<?>, Listeners> listenerMap = new ConcurrentHashMap<>();
	private final Map<Class<?>, Class<?>[]> dispatchTypes = new ConcurrentHashMap<>();

	public FabricZetaEventBus(Class<? extends Annotation> subscriberAnnotation, Class<E> eventRoot, @Nullable Logger logSpam, Zeta ofZeta) {
		super(subscriberAnnotation, eventRoot, logSpam, ofZeta);
	}

	@Override
	protected void subscribeMethod(Method m, Object receiver, Class<?> owningClazz) {
		getListenersFor(m).subscribe(receiver, owningClazz, m);
	}

	@Override
	protected void unsubscribeMethod(Method m, Object receiver, Class<?> owningClazz) {
		getListenersFor(m).unsubscribe(receiver, owningClazz, m);
	}

	@Override
	public <T extends E> T fire(@NotNull T event) {
		Class<?> clazz = event.getClass();
		Listeners exact = listenerMap.get(clazz);
		if(exact != null) {
			exact.doFire(event);
			return event;
		}

		for(Class<?> type : dispatchTypes.computeIfAbsent(clazz, this::resolveDispatchTypes)) {
			Listeners subs = listenerMap.get(type);
			if(subs != null) {
				subs.doFire(event);
				if(event instanceof Cancellable c && c.isCanceled())
					break;
			}
		}
		return event;
	}

	@Override
	public <T extends E> T fire(@NotNull T event, Class<? super T> firedAs) {
		Listeners subs = listenerMap.get(firedAs);
		if(subs != null)
			subs.doFire(event);
		return event;
	}

	/**
	 * @return whether anything listens for this exact event type. Lets hooks skip building expensive events.
	 */
	public boolean hasListeners(Class<?> type) {
		Listeners subs = listenerMap.get(type);
		return subs != null && subs.handles.length > 0;
	}

	private Class<?>[] resolveDispatchTypes(Class<?> clazz) {
		List<Class<?>> types = new ArrayList<>();
		for(Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass())
			for(Class<?> iface : c.getInterfaces())
				if(iface != eventRoot && eventRoot.isAssignableFrom(iface) && !types.contains(iface))
					types.add(iface);
		return types.toArray(new Class<?>[0]);
	}

	private Listeners getListenersFor(Method method) {
		if(method.getParameterCount() != 1)
			throw arityERR(method);

		Class<?> eventType = method.getParameterTypes()[0];
		if(!eventRoot.isAssignableFrom(eventType))
			throw typeERR(method);

		return listenerMap.computeIfAbsent(eventType, __ -> new Listeners());
	}

	private record Subscriber(@Nullable Object receiver, Class<?> owningClazz, Method method) {
		@Override
		public boolean equals(Object object) {
			if(this == object) return true;
			if(!(object instanceof Subscriber that)) return false;
			return receiver == that.receiver && //<-- object identity compare
				Objects.equals(owningClazz, that.owningClazz) &&
				Objects.equals(method, that.method);
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(receiver) + owningClazz.hashCode() + method.hashCode();
		}
	}

	private final class Listeners {

		private volatile Subscriber[] subscribers = new Subscriber[0];
		private volatile MethodHandle[] handles = new MethodHandle[0];

		synchronized void subscribe(@Nullable Object receiver, Class<?> owningClazz, Method method) {
			Subscriber sub = new Subscriber(receiver, owningClazz, method);
			if(Arrays.asList(subscribers).contains(sub))
				return;

			MethodHandle handle;
			try {
				handle = MethodHandles.publicLookup().unreflect(method);
			} catch (Exception e) {
				throw unreflectERR(method, e);
			}
			if(receiver != null)
				handle = handle.bindTo(receiver);

			Subscriber[] newSubs = Arrays.copyOf(subscribers, subscribers.length + 1);
			MethodHandle[] newHandles = Arrays.copyOf(handles, handles.length + 1);
			newSubs[newSubs.length - 1] = sub;
			newHandles[newHandles.length - 1] = handle;
			subscribers = newSubs;
			handles = newHandles;
		}

		synchronized void unsubscribe(@Nullable Object receiver, Class<?> owningClazz, Method method) {
			int idx = Arrays.asList(subscribers).indexOf(new Subscriber(receiver, owningClazz, method));
			if(idx < 0)
				return;

			List<Subscriber> newSubs = new ArrayList<>(Arrays.asList(subscribers));
			List<MethodHandle> newHandles = new ArrayList<>(Arrays.asList(handles));
			newSubs.remove(idx);
			newHandles.remove(idx);
			subscribers = newSubs.toArray(new Subscriber[0]);
			handles = newHandles.toArray(new MethodHandle[0]);
		}

		void doFire(Object event) {
			MethodHandle[] snapshot = handles;
			if(logSpam != null && snapshot.length > 0)
				logSpam.info("Dispatching {} to {} listener{}", event.getClass().getSimpleName(), snapshot.length, snapshot.length > 1 ? "s" : "");

			try {
				if(event instanceof Cancellable cancellable) {
					for(MethodHandle handle : snapshot) {
						handle.invoke(event);
						if(cancellable.isCanceled())
							break;
					}
				} else {
					for(MethodHandle handle : snapshot)
						handle.invoke(event);
				}
			} catch (Throwable e) {
				throw new RuntimeException("Exception while firing event " + event + ": ", e);
			}
		}
	}
}
