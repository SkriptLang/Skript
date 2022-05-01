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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader.ScriptInfo;
import ch.njol.skript.command.Commands;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.util.NonNullPair;

/**
 * @author Peter Güttinger
 */
public abstract class SkriptEventHandler {

	/**
	 * A event listener for one priority.
	 * Also stores the registered events for this listener, and
	 * the {@link EventExecutor} to be used with this listener.
	 */
	public static class PriorityListener implements Listener {
		
		public final EventPriority priority;
		public final Set<Class<? extends Event>> registeredEvents = new HashSet<>();
		
		@Nullable
		private Event lastEvent;
		public final EventExecutor executor = (listener, event) -> {
			if (lastEvent == event) // an event is received multiple times if multiple superclasses of it are registered
				return;
			lastEvent = event;
			check(event, ((PriorityListener) listener).priority);
		};
		
		public PriorityListener(EventPriority priority) {
			this.priority = priority;
		}
		
	}
	
	/**
	 * Stores one {@link PriorityListener} per {@link EventPriority}
	 */
	private static final PriorityListener[] listeners;
	
	static {
		EventPriority[] priorities = EventPriority.values();
		listeners = new PriorityListener[priorities.length];
		for (int i = 0; i < priorities.length; i++) {
			listeners[i] = new PriorityListener(priorities[i]);
		}
	}
	
	private static final List<NonNullPair<Class<? extends Event>, Trigger>> triggers = new ArrayList<>();
	
	private static final List<Trigger> selfRegisteredTriggers = new ArrayList<>();
	
	private static Iterator<Trigger> getTriggers(Class<? extends Event> event) {
		return new ArrayList<>(triggers).stream()
			.filter(pair -> pair.getFirst().isAssignableFrom(event))
			.map(NonNullPair::getSecond)
			.iterator();
	}
	
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
		
		if (e instanceof Cancellable && ((Cancellable) e).isCancelled() && !listenCancelled.contains(e.getClass()) &&
				!(e instanceof PlayerInteractEvent && (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR) && ((PlayerInteractEvent) e).useItemInHand() != Result.DENY)
				|| e instanceof ServerCommandEvent && (((ServerCommandEvent) e).getCommand().isEmpty() || ((ServerCommandEvent) e).isCancelled())) {
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
	
	public static void logEventStart(Event e) {
		startEvent = System.nanoTime();
		if (!Skript.logVeryHigh())
			return;
		Skript.info("");
		Skript.info("== " + e.getClass().getName() + " ==");
	}
	
	public static void logEventEnd() {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("== took " + 1. * (System.nanoTime() - startEvent) / 1000000. + " milliseconds ==");
	}
	
	private static long startTrigger;
	
	public static void logTriggerStart(Trigger t) {
		startTrigger = System.nanoTime();
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName());
	}
	
	public static void logTriggerEnd(Trigger t) {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName() + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
	}

	public static void addTrigger(Class<? extends Event>[] events, Trigger trigger) {
		for (Class<? extends Event> e : events) {
			triggers.add(new NonNullPair<>(e, trigger));
		}
	}
	
	/**
	 * Stores a self registered trigger to allow for it to be unloaded later on.
	 * 
	 * @param t Trigger that has already been registered to its event
	 */
	public static void addSelfRegisteringTrigger(Trigger t) {
		assert t.getEvent() instanceof SelfRegisteringSkriptEvent;
		selfRegisteredTriggers.add(t);
	}
	
	static ScriptInfo removeTriggers(File script) {
		ScriptInfo info = new ScriptInfo();
		info.files = 1;
		
		int previousSize = triggers.size();
		triggers.removeIf(pair -> script.equals(pair.getSecond().getScript()));
		info.triggers += previousSize - triggers.size();
		
		for (int i = 0; i < selfRegisteredTriggers.size(); i++) {
			Trigger t = selfRegisteredTriggers.get(i);
			if (script.equals(t.getScript())) {
				info.triggers++;
				((SelfRegisteringSkriptEvent) t.getEvent()).unregister(t);
				selfRegisteredTriggers.remove(i);
				i--;
			}
		}
		
		info.commands = Commands.unregisterCommands(script);
		
		return info;
	}
	
	static void removeAllTriggers() {
		triggers.clear();
		for (Trigger t : selfRegisteredTriggers)
			((SelfRegisteringSkriptEvent) t.getEvent()).unregisterAll();
		selfRegisteredTriggers.clear();
	}
	
	/**
	 * Registers event handlers for all events which currently loaded
	 * triggers are using.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	static void registerBukkitEvents() {
		for (NonNullPair<Class<? extends Event>, Trigger> pair : triggers) {
			assert pair.getFirst() != null;
			Class<? extends Event> e = pair.getFirst();
			
			EventPriority priority;
			priority = pair.getSecond().getEvent().getEventPriority();
			
			PriorityListener listener = listeners[priority.ordinal()];
			EventExecutor executor = listener.executor;
			
			Set<Class<? extends Event>> registeredEvents = listener.registeredEvents;
			
			// PlayerInteractEntityEvent has a subclass we need for armor stands
			if (e.equals(PlayerInteractEntityEvent.class)) {
				if (!registeredEvents.contains(e)) {
					registeredEvents.add(e);
					Bukkit.getPluginManager().registerEvent(e, listener, priority, executor, Skript.getInstance());
					Bukkit.getPluginManager().registerEvent(PlayerInteractAtEntityEvent.class, listener, priority, executor, Skript.getInstance());
				}
				continue;
			}
			if (e.equals(PlayerInteractAtEntityEvent.class) || e.equals(PlayerArmorStandManipulateEvent.class)) {
				continue; // Ignore, registered above
			}
			
			if (!containsSuperclass((Set) registeredEvents, e)) { // I just love Java's generics
				Bukkit.getPluginManager().registerEvent(e, listener, priority, executor, Skript.getInstance());
				registeredEvents.add(e);
			}
		}
	}
	
	public static boolean containsSuperclass(Set<Class<?>> classes, Class<?> c) {
		if (classes.contains(c))
			return true;
		for (Class<?> cl : classes) {
			if (cl.isAssignableFrom(c))
				return true;
		}
		return false;
	}

	/**
	 * Events which are listened even if they are cancelled.
	 */
	public static final Set<Class<? extends Event>> listenCancelled = new HashSet<>();
	
}
