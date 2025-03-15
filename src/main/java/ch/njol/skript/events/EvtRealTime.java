package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EvtRealTime extends SkriptEvent {

	public static class RealTimeEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	private static final long HOUR_24_MILLISECONDS = 1000 * 60 * 60 * 24;
	private static final Timer TIMER;

	static {
		Skript.registerEvent("System Time", EvtRealTime.class, RealTimeEvent.class,
			"at %times% [in] real time")
				.description("Called when the local time of the system the server is running on reaches the provided real-life time.")
				.examples(
					"at 14:20 in real time:",
					"at 2:30am real time:",
					"at 6:10 pm in real time:",
					"at 5:00 am and 5:00 pm in real time:",
					"at 5:00 and 17:00 in real time:"
				)
				.since("INSERT VERSION");

		TIMER = new Timer("EvtSystemTime-Tasks");
	}

	private Literal<Time> times;
	private boolean unloaded = false;
	private final List<TimerTask> timerTasks = new ArrayList<>();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		times = (Literal<Time>) args[0];
		return true;
	}

	@Override
	public boolean postLoad() {
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTimeZone(TimeZone.getDefault());
		for (Time time : times.getArray()) {
			Calendar expectedCalendar = Calendar.getInstance();
			expectedCalendar.setTimeZone(TimeZone.getDefault());
			expectedCalendar.set(Calendar.MILLISECOND, 0);
			expectedCalendar.set(Calendar.SECOND, 0);
			expectedCalendar.set(Calendar.MINUTE, time.getMinute());
			expectedCalendar.set(Calendar.HOUR_OF_DAY, time.getHour());
			// Ensure the execution time is in the future and not the past
			while (expectedCalendar.before(currentCalendar)) {
				expectedCalendar.add(Calendar.HOUR_OF_DAY, 24);
			}
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					execute();
				}
			};
			timerTasks.add(task);
			TIMER.scheduleAtFixedRate(task, new Date(expectedCalendar.getTimeInMillis()), HOUR_24_MILLISECONDS);
		}
		return true;
	}

	@Override
	public void unload() {
		unloaded = true;
		for (TimerTask task : timerTasks)
			task.cancel();
		TIMER.purge();
	}

	@Override
	public boolean check(Event event) {
		throw new UnsupportedOperationException();
	}

	private void execute() {
		// Ensure this element wasn't unloaded
		if (unloaded)
			return;
		RealTimeEvent event = new RealTimeEvent();
		SkriptEventHandler.logEventStart(event);
		SkriptEventHandler.logTriggerStart(trigger);
		trigger.execute(event);
		SkriptEventHandler.logTriggerEnd(trigger);
		SkriptEventHandler.logEventEnd();
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "at " + times.toString(event, debug) + " in real time";
	}

}
