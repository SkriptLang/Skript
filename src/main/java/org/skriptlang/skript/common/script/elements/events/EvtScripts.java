package org.skriptlang.skript.common.script.elements.events;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EvtScripts extends SkriptEvent {

	@ApiStatus.Internal
	public static class ScriptsEvent extends Event {

		public Collection<Script> scripts;

		private ScriptsEvent(Collection<Script> scripts) {
			this.scripts = scripts;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new UnsupportedOperationException();
		}
	}

	@ApiStatus.Internal
	public static class ScriptsLoadEvent extends ScriptsEvent {

		private ScriptsLoadEvent(Collection<Script> scripts) {
			super(scripts);
		}

	}

	@ApiStatus.Internal
	public static class ScriptsUnloadEvent extends ScriptsEvent {

		private ScriptsUnloadEvent(Collection<Script> scripts) {
			super(scripts);
		}

	}

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtScripts.class, "Scripts Loading")
			.supplier(() -> new EvtScripts(true))
			.addEvent(ScriptsLoadEvent.class)
			.addPattern("scripts (loading|initializing|enabling)")
			.addDescription("""
				Called directly after a batch of scripts is loaded. \
				This is called immediately after individual script load events for the loading scripts.
				""")
			.addExample("""
				on scripts loading:
					if all of the loaded scripts are the same as the loading scripts:
						send "<lime>All scripts have finished loading!" to all operators
				""")
			.addSince("INSERT VERSION")
			.build());
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtScripts.class, "Scripts Unloading")
			.supplier(() -> new EvtScripts(false))
			.addEvent(ScriptsUnloadEvent.class)
			.addPattern("scripts (unloading|stopping|disabling)")
			.addDescription("""
				Called directly before a batch of scripts is unloaded.
				This is called immediately after individual script unload events for the unloading scripts.
				""")
			.addExample("""
				on scripts unloading:
					if any of the names of the unloading scripts contain "important":
						send "<red>[!] An important script is unloading!" to all operators
				""")
			.addSince("INSERT VERSION")
			.build());

		ScriptLoader.eventRegistry().register(ScriptLoader.ScriptsLoadEvent.class, (ignored, scripts) -> {
			ScriptsEvent event = new ScriptsLoadEvent(scripts);
			if (Bukkit.isPrimaryThread()) {
				LOAD_TRIGGERS.forEach(trigger -> trigger.execute(event));
			} else {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(),
					() -> LOAD_TRIGGERS.forEach(trigger -> trigger.execute(event)));
			}
		});
		ScriptLoader.eventRegistry().register(ScriptLoader.ScriptsUnloadEvent.class, (ignored, scripts) -> {
			ScriptsEvent event = new ScriptsUnloadEvent(scripts);
			if (Bukkit.isPrimaryThread()) {
				UNLOAD_TRIGGERS.forEach(trigger -> trigger.execute(event));
			} else {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(),
					() -> UNLOAD_TRIGGERS.forEach(trigger -> trigger.execute(event)));
			}
		});
	}

	private static final Set<Trigger> LOAD_TRIGGERS = ConcurrentHashMap.newKeySet();
	private static final Set<Trigger> UNLOAD_TRIGGERS = ConcurrentHashMap.newKeySet();

	private final boolean load;

	private EvtScripts(boolean load) {
		this.load = load;
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean postLoad() {
		if (load) {
			LOAD_TRIGGERS.add(trigger);
		} else {
			UNLOAD_TRIGGERS.add(trigger);
		}
		return true;
	}

	@Override
	public void unload() {
		if (load) {
			LOAD_TRIGGERS.remove(trigger);
		} else {
			UNLOAD_TRIGGERS.remove(trigger);
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

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "scripts " + (load ? "" : "un") + "loading";
	}

}
