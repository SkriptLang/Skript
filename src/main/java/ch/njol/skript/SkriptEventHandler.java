/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.util.NonNullPair;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SkriptEventHandler {

	private SkriptEventHandler() { }

	/**
	 * An event listener for one priority.
	 * Also stores the registered events for this listener, and
	 * the {@link EventExecutor} to be used with this listener.
	 */
	public static class PriorityListener implements Listener {

		public final EventPriority priority;

		public final EventExecutor executor = (listener, event) -> check(event, ((PriorityListener) listener).priority);

		public PriorityListener(EventPriority priority) {
			this.priority = priority;
		}

	}

	/**
	 * Stores one {@link PriorityListener} per {@link EventPriority}.
	 */
	private static final PriorityListener[] listeners;

	static {
		EventPriority[] priorities = EventPriority.values();
		listeners = new PriorityListener[priorities.length];
		for (int i = 0; i < priorities.length; i++) {
			listeners[i] = new PriorityListener(priorities[i]);
		}
	}

	/**
	 * A list tracking what Triggers are paired with what Events.
	 */
	private static final List<NonNullPair<Class<? extends Event>, Trigger>> triggers = new ArrayList<>();

	/**
	 * A utility method to get all Triggers paired with the provided Event class.
	 * @param event The event to find pairs from.
	 * @return An iterator containing all Triggers paired with the provided Event class.
	 */
	private static Iterator<Trigger> getTriggers(Class<? extends Event> event) {
		HandlerList eventHandlerList = getHandlerList(event);
		assert eventHandlerList != null; // It had one at some point so this should remain true
		return new ArrayList<>(triggers).stream()
			.filter(pair -> pair.getFirst().isAssignableFrom(event) && eventHandlerList == getHandlerList(pair.getFirst()))
			.map(NonNullPair::getSecond)
			.iterator();
	}

	/**
	 * This method is used for validating that the provided Event may be handled by Skript.
	 * If validation is successful, all Triggers associated with the provided Event are executed.
	 * A Trigger will only be executed if its priority matches the provided EventPriority.
	 * @param e The Event to check.
	 * @param priority The priority of the Event.
	 */
	private static void check(Event e, EventPriority priority) {
		Iterator<Trigger> ts = getTriggers(e.getClass());
		if (!ts.hasNext())
			return;

		if (Skript.logVeryHigh()) {
			boolean hasTrigger = false;
			while (ts.hasNext()) {
				Trigger trigger = ts.next();
				if (trigger.getEvent().getEventPriority() == priority && trigger.getEvent().check(e)) {
					hasTrigger = true;
					break;
				}
			}
			if (!hasTrigger)
				return;
			Class<? extends Event> c = e.getClass();
			ts = getTriggers(c);

			logEventStart(e);
		}
		
		boolean isCancelled = e instanceof Cancellable && ((Cancellable) e).isCancelled() && !listenCancelled.contains(e.getClass());
		boolean isResultDeny = !(e instanceof PlayerInteractEvent && (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR) && ((PlayerInteractEvent) e).useItemInHand() != Result.DENY);

		if (isCancelled && isResultDeny) {
			if (Skript.logVeryHigh())
				Skript.info(" -x- was cancelled");
			return;
		}

		while (ts.hasNext()) {
			Trigger t = ts.next();
			if (t.getEvent().getEventPriority() != priority || !t.getEvent().check(e))
				continue;

			logTriggerStart(t);
			Object timing = SkriptTimings.start(t.getDebugLabel());

			t.execute(e);

			SkriptTimings.stop(timing);
			logTriggerEnd(t);
		}

		logEventEnd();
	}

	private static long startEvent;

	/**
	 * Logs that the provided Event has started.
	 * Requires {@link Skript#logVeryHigh()} to be true to log anything.
	 * @param event The Event that started.
	 */
	public static void logEventStart(Event event) {
		startEvent = System.nanoTime();
		if (!Skript.logVeryHigh())
			return;
		Skript.info("");
		Skript.info("== " + event.getClass().getName() + " ==");
	}

	/**
	 * Logs that the last logged Event start has ended.
	 * Includes the number of milliseconds execution took.
	 * Requires {@link Skript#logVeryHigh()} to be true to log anything.
	 */
	public static void logEventEnd() {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("== took " + 1. * (System.nanoTime() - startEvent) / 1000000. + " milliseconds ==");
	}

	private static long startTrigger;

	/**
	 * Logs that the provided Trigger has begun execution.
	 * Requires {@link Skript#logVeryHigh()} to be true.
	 * @param trigger The Trigger that execution has begun for.
	 */
	public static void logTriggerStart(Trigger trigger) {
		startTrigger = System.nanoTime();
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + trigger.getName());
	}

	/**
	 * Logs that the last logged Trigger execution has ended.
	 * Includes the number of milliseconds execution took.
	 * Requires {@link Skript#logVeryHigh()} to be true to log anything.
	 */
	public static void logTriggerEnd(Trigger t) {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName() + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
	}

	/**
	 * @deprecated This method no longer does anything as self registered Triggers
	 * 	are unloaded when the {@link ch.njol.skript.lang.SkriptEvent} is unloaded (no need to keep tracking them here).
	 */
	@Deprecated
	public static void addSelfRegisteringTrigger(Trigger t) { }

	/**
	 * A utility method that calls {@link #registerBukkitEvent(Trigger, Class)} for each Event class provided.
	 * For specific details of the process, see the javadoc of that method.
	 * @param trigger The Trigger to run when the Event occurs.
	 * @param events The Event to listen for.
	 * @see #registerBukkitEvent(Trigger, Class)
	 * @see #unregisterBukkitEvents(Trigger)
	 */
	public static void registerBukkitEvents(Trigger trigger, Class<? extends Event>[] events) {
		for (Class<? extends Event> event : events)
			registerBukkitEvent(trigger, event);
	}

	/**
	 * Registers a {@link PriorityListener} with Bukkit for the provided Event.
	 * Marks that the provided Trigger should be executed when the provided Event occurs.
	 * @param trigger The Trigger to run when the Event occurs.
	 * @param event The Event to listen for.
	 * @see #registerBukkitEvents(Trigger, Class[])
	 * @see #unregisterBukkitEvents(Trigger)
	 */
	public static void registerBukkitEvent(Trigger trigger, Class<? extends Event> event) {
		HandlerList handlerList = getHandlerList(event);
		if (handlerList == null)
			return;

		triggers.add(new NonNullPair<>(event, trigger));

		EventPriority priority = trigger.getEvent().getEventPriority();

		if (!isEventRegistered(handlerList, priority)) { // Check if event is registered
			PriorityListener listener = listeners[priority.ordinal()];
			Bukkit.getPluginManager().registerEvent(event, listener, priority, listener.executor, Skript.getInstance());
		}
	}

	/**
	 * Unregisters all events tied to the provided Trigger.
	 * @param trigger The Trigger to unregister events for.
	 */
	public static void unregisterBukkitEvents(Trigger trigger) {
		triggers.removeIf(pair -> {
			if (pair.getSecond() != trigger)
				return false;

			HandlerList handlerList = getHandlerList(pair.getFirst());
			assert handlerList != null;

			EventPriority priority = trigger.getEvent().getEventPriority();
			if (triggers.stream().noneMatch(pair2 ->
				trigger != pair2.getSecond() // Don't match the trigger we are unregistering
				&& pair2.getFirst().isAssignableFrom(pair.getFirst()) // Basic similarity check
				&& priority == pair2.getSecond().getEvent().getEventPriority() // Ensure same priority
				&& handlerList == getHandlerList(pair2.getFirst()) // Ensure same handler list
			)) { // We can attempt to unregister this listener
				Skript skript = Skript.getInstance();
				for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
					Listener listener = registeredListener.getListener();
					if (
						registeredListener.getPlugin() == skript
						&& listener instanceof PriorityListener
						&& ((PriorityListener) listener).priority == priority
					) {
						handlerList.unregister(listener);
					}
				}
			}

			return true;
		});
	}

	/**
	 * Events which are listened even if they are cancelled.
	 */
	public static final Set<Class<? extends Event>> listenCancelled = new HashSet<>();

	/**
	 * A cache for the getHandlerList methods of Event classes.
	 */
	private static final Map<Class<? extends Event>, Method> handlerListMethods = new HashMap<>();

	/**
	 * A cache for obtained HandlerLists.
	 */
	private static final Map<Method, WeakReference<HandlerList>> handlerListCache = new HashMap<>();

	@Nullable
	private static HandlerList getHandlerList(Class<? extends Event> eventClass) {
		try {
			Method method = getHandlerListMethod(eventClass);

			WeakReference<HandlerList> handlerListReference = handlerListCache.get(method);
			HandlerList handlerList = handlerListReference != null ? handlerListReference.get() : null;
			if (handlerList == null) {
				method.setAccessible(true);
				handlerList = (HandlerList) method.invoke(null);
				handlerListCache.put(method, new WeakReference<>(handlerList));
			}

			return handlerList;
		} catch (Exception ex) {
			//noinspection ThrowableNotThrown
			Skript.exception(ex, "Failed to get HandlerList for event " + eventClass.getName());
			return null;
		}
	}

	private static Method getHandlerListMethod(Class<? extends Event> eventClass) {
		Method method;

		synchronized (handlerListMethods) {
			method = handlerListMethods.get(eventClass);
			if (method == null) {
				method = getHandlerListMethod_i(eventClass);
				if (method != null)
					method.setAccessible(true);
				handlerListMethods.put(eventClass, method);
			}
		}

		if (method == null)
			throw new RuntimeException("No getHandlerList method found");

		return method;
	}

	@Nullable
	private static Method getHandlerListMethod_i(Class<? extends Event> eventClass) {
		try {
			return eventClass.getDeclaredMethod("getHandlerList");
		} catch (NoSuchMethodException e) {
			if (
				eventClass.getSuperclass() != null
				&& !eventClass.getSuperclass().equals(Event.class)
				&& Event.class.isAssignableFrom(eventClass.getSuperclass())
			) {
				return getHandlerListMethod(eventClass.getSuperclass().asSubclass(Event.class));
			} else {
				return null;
			}
		}
	}

	private static boolean isEventRegistered(HandlerList handlerList, EventPriority priority) {
		for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
			Listener listener = registeredListener.getListener();
			if (
				registeredListener.getPlugin() == Skript.getInstance()
				&& listener instanceof PriorityListener
				&& ((PriorityListener) listener).priority == priority
			) {
				return true;
			}
		}
		return false;
	}

}
