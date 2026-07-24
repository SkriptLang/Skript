package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ScheduledEvent;
import ch.njol.skript.events.bukkit.ScheduledNoWorldEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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
	private ScheduledTask[] scheduledTasks;

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
			scheduledTasks = new ScheduledTask[]{
				Skript.getScheduler().runGlobalRepeatingTask(
					() -> execute(null), ticks, ticks
				)
			};
		} else {
			scheduledTasks = new ScheduledTask[worlds.length];
			for (int i = 0; i < worlds.length; i++) {
				World world = worlds[i];
				scheduledTasks[i] = Skript.getScheduler().runGlobalRepeatingTask(
					() -> execute(world), ticks - (world.getFullTime() % ticks), ticks
				);
			}
		}

		return true;
	}

	@Override
	public void unload() {
		for (ScheduledTask taskID : scheduledTasks)
			taskID.cancel();
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
