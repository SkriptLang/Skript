package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.*;

public class EvtRealTime extends SkriptEvent {

	private static final long HOUR_24_MILLISECONDS = 1000 * 60 * 60 * 24;
	private static Timer TIMER;

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtRealTime.class, "System Time")
			.supplier(EvtRealTime::new)
			.addEvent(RealTimeEvent.class)
			.addPatterns("at %times% [in] real time")
			.addDescription("Called when the local time of the system the server is running on reaches the provided real-life time.")
			.addExample("""
				at 2:30am in real time:
					broadcast "Time to get some sleep.."
				""")
			.addExample("""
				at 12am real time:
					resetDailyQuests()
					broadcast "All daily quests have reset!"
				""")
			.addSince("2.11")
			.build());

		TIMER = new Timer("EvtSystemTime-Tasks");
	}

	private Literal<Time> times;
	private boolean unloaded = false;
	private final List<TimerTask> timerTasks = new ArrayList<>();

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
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

		Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
			RealTimeEvent event = new RealTimeEvent();
			SkriptEventHandler.logEventStart(event);
			SkriptEventHandler.logTriggerStart(trigger);
			trigger.execute(event);
			SkriptEventHandler.logTriggerEnd(trigger);
			SkriptEventHandler.logEventEnd();
		});
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "at " + times.toString(event, debug) + " in real time";
	}

	public static class RealTimeEvent extends Event {

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

}
