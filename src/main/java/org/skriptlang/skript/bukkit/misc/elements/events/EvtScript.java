package org.skriptlang.skript.bukkit.misc.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtScript extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtScript.class, "Script Load/Unload")
			.supplier(EvtScript::new)
			.addEvent(ScriptEvent.class)
			.addPatterns(
				"[:async] [script] (load|init|enable)",
				"[:async] [script] (unload|stop|disable)"
			)
			.addDescription("""
				Called directly after the trigger is loaded, or directly before the whole script is unloaded.
				The keyword 'async' indicates the trigger can be ran asynchronously.
				""")
			.addExample("""
				on script load:
				    set {running::%script%} to true
				    broadcast "%script% is now running!"
				""")
			.addExample("""
				on script unload:
				    set {running::%script%} to false
				    broadcast "%script% is no longer running!"
				""")
			.addSince("2.0")
			.build());

		eventValueRegistry.register(EventValue.builder(ScriptEvent.class, CommandSender.class)
			.getter(event -> Bukkit.getConsoleSender())
			.build());
	}

	private boolean async;
	private boolean load;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		async = parseResult.hasTag("async");
		load = matchedPattern == 0;
		return true;
	}

	@Override
	public boolean postLoad() {
		if (load)
			runTrigger(trigger, new ScriptEvent());
		return true;
	}

	@Override
	public void unload() {
		if (!load)
			runTrigger(trigger, new ScriptEvent());
	}

	@Override
	public boolean check(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

	private void runTrigger(Trigger trigger, Event event) {
		if (async || Bukkit.isPrimaryThread()) {
			trigger.execute(event);
		} else {
			if (Skript.getInstance().isEnabled())
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> trigger.execute(event));
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (async ? "async " : "") + "script " + (load ? "" : "un") + "load";
	}

}
