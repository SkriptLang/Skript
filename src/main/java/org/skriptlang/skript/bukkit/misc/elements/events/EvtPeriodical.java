package org.skriptlang.skript.bukkit.misc.elements.events;

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
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPeriodical extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPeriodical.class, "Periodical")
			.supplier(EvtPeriodical::new)
			.addEvent(ScheduledNoWorldEvent.class)
			.addPattern("every %timespan%")
			.addDescription("An event that is called periodically.")
			.addExample("""
				every 2 seconds:
					send actionbar "hello!" to all players
				""")
			.addExample("""
				every minecraft hour:
					broadcast "Another hour has passed in this virtual world.."
				""")
			.documentationId("eventperiodical")
			.addSince("1.0")
			.build());

		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPeriodical.class, "Periodical")
			.supplier(EvtPeriodical::new)
			.addEvent(ScheduledEvent.class)
			.addPattern("every %timespan% in [world[s]] %worlds%")
			.addDescription("An event that is called periodically.")
			.addExample("""
				every 2 seconds in "adminworld":
					send actionbar "<red>Hi admins!" to all players
				""")
			.addExample("""
				every tick in "superflat":
					add 1 to {-ticks}
					if {bar} is not set:
						set {bar} to a boss bar:
							set title of event-boss bar to "Existence: 0 ticks"
							add all players in event-world to viewers of event-boss bar
				else:
					set title of {bar} to "Existence: %{-ticks}% ticks"
				""")
			.documentationId("eventperiodical")
			.addSince("1.0")
			.build());

		eventValueRegistry.register(EventValue.builder(ScheduledEvent.class, World.class)
			.getter(ScheduledEvent::getWorld)
			.excludes(ScheduledNoWorldEvent.class)
			.excludedErrorMessage("There's no world in a periodic event if no world is given in the event (e.g. like 'every hour in \\\"world\\\"')")
			.build());
	}

	private Timespan period;

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

	private void execute(@Nullable World world) {
		ScheduledEvent event = world == null ? new ScheduledNoWorldEvent() : new ScheduledEvent(world);
		SkriptEventHandler.logEventStart(event);
		SkriptEventHandler.logTriggerStart(trigger);
		trigger.execute(event);
		SkriptEventHandler.logTriggerEnd(trigger);
		SkriptEventHandler.logEventEnd();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "every " + period;
	}

}
