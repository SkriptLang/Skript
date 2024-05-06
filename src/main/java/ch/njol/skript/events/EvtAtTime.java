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
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ScheduledEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Time;
import ch.njol.util.Math2;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class EvtAtTime extends SkriptEvent implements Comparable<EvtAtTime> {

	static {
		Skript.registerEvent("*At Time", EvtAtTime.class, ScheduledEvent.class, "at %time% [in %worlds%]")
				.description("An event that occurs at a given <a href='./classes.html#time'>minecraft time</a> in every world or only in specific worlds.")
				.examples("at 18:00", "at 7am in \"world\"")
				.since("1.3.4");
	}
	
	private static final int CHECK_PERIOD = 10;

	private static final Map<World, EvtAtInfo> TRIGGERS = new ConcurrentHashMap<>();

	private static final class EvtAtInfo {
		/**
		 * Stores the last world time that this object's instances were checked.
		 */
		private int lastCheckedTime;

		/**
		 * A list of all {@link EvtAtTime}s in the world this info object is responsible for.
		 * Sorted by the time they're listening for in increasing order.
		 */
		private final PriorityQueue<EvtAtTime> instances = new PriorityQueue<>(EvtAtTime::compareTo);
	}

	private int time;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private World[] worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		time = ((Literal<Time>) args[0]).getSingle().getTicks();
		worlds = args[1] == null ? Bukkit.getWorlds().toArray(new World[0]) : ((Literal<World>) args[1]).getAll();
		return true;
	}

	@Override
	public boolean postLoad() {
		for (World world : worlds) {
			EvtAtInfo info = TRIGGERS.get(world);
			if (info == null) {
				TRIGGERS.put(world, info = new EvtAtInfo());
				info.lastCheckedTime = (int) world.getTime() - 1;
			}
			info.instances.add(this);
		}
		registerListener();
		return true;
	}

	@Override
	public void unload() {
		Iterator<EvtAtInfo> iterator = TRIGGERS.values().iterator();
		while (iterator.hasNext()) {
			EvtAtInfo info = iterator.next();
			info.instances.remove(this);
			if (info.instances.isEmpty())
				iterator.remove();
		}

		if (taskID != -1 && TRIGGERS.isEmpty()) { // Unregister Bukkit listener if possible
			Bukkit.getScheduler().cancelTask(taskID);
			taskID = -1;
		}
	}

	@Override
	public boolean check(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

	private static int taskID = -1;
	
	private static void registerListener() {
		if (taskID != -1)
			return;
		// For each world:
		// check each instance in order until triggerTime > (worldTime + period)
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> {
			for (Entry<World, EvtAtInfo> entry : TRIGGERS.entrySet()) {
				EvtAtInfo info = entry.getValue();
				int worldTime = (int) entry.getKey().getTime();

				// Stupid Bukkit scheduler
				// TODO: is this really necessary?
				if (info.lastCheckedTime == worldTime)
					continue;

				// Check if time changed, e.g. by a command or plugin
				// if the info was last checked more than 2 cycles ago
				// then reset the last checked time to the period just before now.
				if (info.lastCheckedTime + CHECK_PERIOD * 2 < worldTime || (info.lastCheckedTime > worldTime && info.lastCheckedTime - 24000 + CHECK_PERIOD * 2 < worldTime))
					info.lastCheckedTime = Math2.mod(worldTime - CHECK_PERIOD, 24000);

				// if we rolled over from 23999 to 0, subtract 24000 from last checked
				boolean midnight = info.lastCheckedTime > worldTime; // actually 6:00
				if (midnight)
					info.lastCheckedTime -= 24000;

				// loop instances from earliest to latest
				for (EvtAtTime event : info.instances) {
					// if we just rolled over, the last checked time will be x - 24000, so we need to do the same to the event time
					int eventTime = midnight && event.time > 12000 ? event.time - 24000 : event.time;

					// if the event time is in the future, we don't need to check any more events.
					if (eventTime > worldTime)
						break;

					// if we should have already caught this time previously, check the next one
					if (eventTime <= info.lastCheckedTime)
						continue;

					// anything that makes it here must satisfy lastCheckedTime < eventTime <= worldTime
					// and therefore should trigger this event.
					ScheduledEvent scheduledEvent = new ScheduledEvent(entry.getKey());
					SkriptEventHandler.logEventStart(scheduledEvent);
					SkriptEventHandler.logTriggerEnd(event.trigger);
					event.trigger.execute(scheduledEvent);
					SkriptEventHandler.logTriggerEnd(event.trigger);
					SkriptEventHandler.logEventEnd();
				}
				info.lastCheckedTime = worldTime;
			}
		}, 0, CHECK_PERIOD);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "at " + Time.toString(time) + " in worlds " + Classes.toString(worlds, true);
	}
	
	@Override
	public int compareTo(@Nullable EvtAtTime event) {
		return event == null ? time : time - event.time;
	}
	
}
