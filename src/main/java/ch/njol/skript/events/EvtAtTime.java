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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
		private int lastTick; // as Bukkit's scheduler is inconsistent this saves the exact tick when the events were last checked
		private int currentIndex;
		private final List<EvtAtTime> instances = new ArrayList<>();
	}

	private int tick;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private World[] worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		tick = ((Literal<Time>) args[0]).getSingle().getTicks();
		worlds = args[1] == null ? Bukkit.getWorlds().toArray(new World[0]) : ((Literal<World>) args[1]).getAll();
		return true;
	}

	@Override
	public boolean postLoad() {
		for (World world : worlds) {
			EvtAtInfo info = TRIGGERS.get(world);
			if (info == null) {
				TRIGGERS.put(world, info = new EvtAtInfo());
				info.lastTick = (int) world.getTime() - 1;
			}
			info.instances.add(this);
			Collections.sort(info.instances);
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
			if (info.currentIndex >= info.instances.size())
				info.currentIndex--;
			if (info.instances.isEmpty())
				iterator.remove();
		}

		if (taskID == -1 && TRIGGERS.isEmpty()) { // Unregister Bukkit listener if possible
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
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> {
			for (Entry<World, EvtAtInfo> entry : TRIGGERS.entrySet()) {
				EvtAtInfo info = entry.getValue();
				int tick = (int) entry.getKey().getTime();

				// Stupid Bukkit scheduler
				if (info.lastTick == tick)
					continue;

				// Check if time changed, e.g. by a command or plugin
				if (info.lastTick + CHECK_PERIOD * 2 < tick || info.lastTick > tick && info.lastTick - 24000 + CHECK_PERIOD * 2 < tick)
					info.lastTick = Math2.mod(tick - CHECK_PERIOD, 24000);

				boolean midnight = info.lastTick > tick; // actually 6:00
				if (midnight)
					info.lastTick -= 24000;

				int startIndex = info.currentIndex;
				while (true) {
					EvtAtTime next = info.instances.get(info.currentIndex);
					int nextTick = midnight && next.tick > 12000 ? next.tick - 24000 : next.tick;

					if (!(info.lastTick < nextTick && nextTick <= tick))
						break;

					// Execute our event
					ScheduledEvent event = new ScheduledEvent(entry.getKey());
					SkriptEventHandler.logEventStart(event);
					SkriptEventHandler.logTriggerEnd(next.trigger);
					next.trigger.execute(event);
					SkriptEventHandler.logTriggerEnd(next.trigger);
					SkriptEventHandler.logEventEnd();

					info.currentIndex++;
					if (info.currentIndex == info.instances.size())
						info.currentIndex = 0;
					if (info.currentIndex == startIndex) // All events executed at once
						break;
				}

				info.lastTick = tick;
			}
		}, 0, CHECK_PERIOD);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "at " + Time.toString(tick) + " in worlds " + Classes.toString(worlds, true);
	}
	
	@Override
	public int compareTo(@Nullable EvtAtTime event) {
		return event == null ? tick : tick - event.tick;
	}
	
}
