package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.SkriptStartEvent;
import ch.njol.skript.events.bukkit.SkriptStopEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvtSkript extends SkriptEvent {

	static {
		Skript.registerEvent("Server Start/Stop", EvtSkript.class, CollectionUtils.array(SkriptStartEvent.class, SkriptStopEvent.class),
				"(:server|skript) (start|load|enable)", "(:server|skript) (stop|unload|disable)"
			)
			.description("Called when the server starts or stops (actually, when Skript starts or stops, so a /reload will trigger these events as well).")
			.examples("on skript start:", "on server stop:")
			.since("2.0");
	}

	private static final List<Trigger> START = Collections.synchronizedList(new ArrayList<>());
	private static final List<Trigger> STOP = Collections.synchronizedList(new ArrayList<>());

	public static void onSkriptStart() {
		Event event = new SkriptStartEvent();
		synchronized (START) {
			for (Trigger trigger : START)
				trigger.execute(event);
			START.clear();
		}
	}

	public static void onSkriptStop() {
		Event event = new SkriptStopEvent();
		synchronized (STOP) {
			for (Trigger trigger : STOP)
				trigger.execute(event);
			STOP.clear();
		}
	}
	
	private boolean isStart;
	
	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		isStart = matchedPattern == 0;
		if (parseResult.hasTag("server"))
			Skript.warning(
				"Server start/stop events are actually called when Skript is started or stopped." +
				"It is thus recommended to use 'on Skript start/stop' instead."
			);
		return true;
	}

	@Override
	public boolean postLoad() {
		(isStart ? START : STOP).add(trigger);
		return true;
	}

	@Override
	public void unload() {
		(isStart ? START : STOP).remove(trigger);
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
		return "on skript " + (isStart ? "start" : "stop");
	}
	
}
