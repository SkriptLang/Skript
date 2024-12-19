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
import ch.njol.skript.events.bukkit.ScheduledNoWorldEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EvtPeriodical extends SkriptEvent {

	static {
		Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledNoWorldEvent.class, "every %timespan%")
				.description("An event that is called periodically.")
				.examples(
					"every 2 seconds:",
					"every minecraft hour:",
					"every tick: # can cause lag depending on the code inside the event",
					"every minecraft days:"
				).since("1.0");
		Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledEvent.class, "every %timespan% in [world[s]] %worlds%")
				.description("An event that is called periodically.")
				.examples(
					"every 2 seconds in \"world\":",
					"every minecraft hour in \"flatworld\":",
					"every tick in \"world\": # can cause lag depending on the code inside the event",
					"every minecraft days in \"plots\":"
				).since("1.0")
				.documentationID("eventperiodical");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Timespan period;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private int[] taskIDs;

	private World @Nullable [] worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		period = ((Literal<Timespan>) args[0]).getSingle();
		if (args.length > 1 && args[1] != null)
			worlds = ((Literal<World>) args[1]).getArray();
		return true;
	}

	@Override
	public boolean postLoad() {
		long ticks = period.getAs(Timespan.TimePeriod.TICK);

		if (worlds == null) {
			taskIDs = new int[]{
				Bukkit.getScheduler().scheduleSyncRepeatingTask(
					Skript.getInstance(), () -> execute(null), ticks, ticks
				)
			};
		} else {
			taskIDs = new int[worlds.length];
			for (int i = 0; i < worlds.length; i++) {
				World world = worlds[i];
				taskIDs[i] = Bukkit.getScheduler().scheduleSyncRepeatingTask(
					Skript.getInstance(), () -> execute(world), ticks - (world.getFullTime() % ticks), ticks
				);
			}
		}

		return true;
	}

	@Override
	public void unload() {
		for (int taskID : taskIDs)
			Bukkit.getScheduler().cancelTask(taskID);
	}

	@Override
	public boolean check(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "every " + period;
	}

	private void execute(@Nullable World world) {
		ScheduledEvent event = world == null ? new ScheduledNoWorldEvent() : new ScheduledEvent(world);
		SkriptEventHandler.logEventStart(event);
		SkriptEventHandler.logTriggerStart(trigger);
		trigger.execute(event);
		SkriptEventHandler.logTriggerEnd(trigger);
		SkriptEventHandler.logEventEnd();
	}
	
}
