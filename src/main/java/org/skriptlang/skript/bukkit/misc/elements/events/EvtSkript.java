package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.SkriptStartEvent;
import ch.njol.skript.events.bukkit.SkriptStopEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvtSkript extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtSkript.class, "Skript Start/Stop")
			.supplier(EvtSkript::new)
			.addEvents(CollectionUtils.array(SkriptStartEvent.class, SkriptStopEvent.class))
			.addPatterns(
				"(:server|skript) (start|load|enable)",
				"(:server|skript) (stop|unload|disable)"
			)
			.addDescription("""
				Called when Skript itself starts or stops.
				Note that reloading a script will trigger these events as well.
				""")
			.addExample("""
				on skript start:
				    kill all entities where [data tag "example" of input is set] # just in case Skript stop didn't catch them for some reason
				    spawn interaction at {mylocation}:
				        set data tag "example" of entity to true
				""")
			.addExample("""
				on skript stop:
				    kill all entities where [data tag "example" of input is set]
				""")
			.addSince("2.0")
			.build());

		eventValueRegistry.register(EventValue.builder(SkriptStartEvent.class, CommandSender.class)
			.getter(event -> Bukkit.getConsoleSender())
			.build());

		eventValueRegistry.register(EventValue.builder(SkriptStopEvent.class, CommandSender.class)
			.getter(event -> Bukkit.getConsoleSender())
			.build());
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
			Skript.warning("""
					Server start/stop events are actually called when Skript is started or stopped.
					It is thus recommended to use 'on Skript start/stop' instead.
					""");
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
