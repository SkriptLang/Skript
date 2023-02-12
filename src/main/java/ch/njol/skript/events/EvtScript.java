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
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class EvtScript extends SkriptEvent {
	
	static {
		Skript.registerEvent("Script Load/Unload", EvtScript.class, ScriptEvent.class,
			"[:async] [script] (load|init|enable)",
			"[:async] [script] (unload|stop|disable)"
			).description(
				"Called directly after the trigger is loaded, or directly before the whole script is unloaded.",
				"The keyword 'async' indicates the trigger can be ran asynchronously, "
			).examples(
				"on load:",
				"\tset {running::%script%} to true",
				"on unload:",
				"\tset {running::%script%} to false"
			).since("2.0");
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
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (async ? "async " : "") + "script " + (load ? "" : "un") + "load";
	}

	private void runTrigger(Trigger trigger, Event event) {
		if (async || Bukkit.isPrimaryThread()) {
			trigger.execute(event);
		} else {
			if (Skript.getInstance().isEnabled())
				Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> trigger.execute(event));
		}
	}
	
}
