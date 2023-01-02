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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ScheduledEvent;
import ch.njol.skript.events.bukkit.ScheduledNoWorldEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.util.Timespan;

/**
 * @author Peter Güttinger
 */
public class EvtPeriodical extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledNoWorldEvent.class, "every %timespan%")
				.description("An event that is called periodically.")
				.examples("every 2 seconds:",
						"every minecraft hour:",
						"every tick: # can cause lag depending on the code inside the event",
						"every minecraft days:")
				.since("1.0");
		Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledEvent.class, "every %timespan% in [world[s]] %worlds%")
				.description("An event that is called periodically.")
				.examples("every 2 seconds in \"world\":",
						"every minecraft hour in \"flatworld\":",
						"every tick in \"world\": # can cause lag depending on the code inside the event",
						"every minecraft days in \"plots\":")
				.since("1.0")
				.documentationID("eventperiodical");
	}
	
	@SuppressWarnings("null")
	private Timespan period;
	
	@Nullable
	private Trigger t;
	@Nullable
	private int[] taskIDs;
	
	@Nullable
	private transient World[] worlds;
	
//	@Nullable
//	private String[] worldNames;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		period = ((Literal<Timespan>) args[0]).getSingle();
		if (args.length > 1 && args[1] != null) {
			worlds = ((Literal<World>) args[1]).getArray();
//			worldNames = new String[worlds.length];
//			for (int i = 0; i < worlds.length; i++)
//				worldNames[i] = worlds[i].getName();
		}
		return true;
	}
	
	void execute(final @Nullable World w) {
		final Trigger t = this.t;
		if (t == null) {
			assert false;
			return;
		}
		final ScheduledEvent e = w == null ? new ScheduledNoWorldEvent() : new ScheduledEvent(w);
		SkriptEventHandler.logEventStart(e);
		SkriptEventHandler.logTriggerStart(t);
		t.execute(e);
		SkriptEventHandler.logTriggerEnd(t);
		SkriptEventHandler.logEventEnd();
	}
	
	@SuppressWarnings("null")
	@Override
	public void register(final Trigger t) {
		this.t = t;
		int[] taskIDs;
		if (worlds == null) {
			taskIDs = new int[] {Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					execute(null);
				}
			}, period.getTicks(), period.getTicks())};
		} else {
			taskIDs = new int[worlds.length];
			for (int i = 0; i < worlds.length; i++) {
				final World w = worlds[i];
				taskIDs[i] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), new Runnable() {
					@Override
					public void run() {
						execute(w);
					}
				}, period.getTicks() - (w.getFullTime() % period.getTicks()), period.getTicks());
				assert worlds != null; // FindBugs
			}
		}
		this.taskIDs = taskIDs;
	}
	
	@Override
	public void unregister(final Trigger t) {
		assert t == this.t;
		this.t = null;
		assert taskIDs != null;
		for (final int taskID : taskIDs)
			Bukkit.getScheduler().cancelTask(taskID);
	}
	
	@Override
	public void unregisterAll() {
		t = null;
		assert taskIDs != null;
		for (final int taskID : taskIDs)
			Bukkit.getScheduler().cancelTask(taskID);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "every " + period;
	}
	
}
