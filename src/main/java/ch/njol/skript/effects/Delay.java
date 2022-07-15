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
package ch.njol.skript.effects;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import ch.njol.skript.sections.EffSecSpawn;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;

@Name("Delay")
@Description("Delays the script's execution by a given timespan. Please note that delays are not persistent, e.g. trying to create a tempban script with <code>ban player → wait 7 days → unban player</code> will not work if you restart your server anytime within these 7 days. You also have to be careful even when using small delays!")
@Examples({
	"wait 2 minutes",
	"halt for 5 minecraft hours",
	"wait a tick"
})
@Since("1.4")
public class Delay extends Effect {

	static {
		Skript.registerEffect(Delay.class, "(wait|halt) [for] %timespan%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	protected Expression<Timespan> duration;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentSection(EffSecSpawn.class)) {
			Skript.error("Delays can't be used within spawn effect sections");
			return false;
		}

		getParser().setHasDelayBefore(Kleenean.TRUE);

		duration = (Expression<Timespan>) exprs[0];
		if (duration instanceof Literal) { // If we can, do sanity check for delays
			long millis = ((Literal<Timespan>) duration).getSingle().getMilliSeconds();
			if (millis < 50) {
				Skript.warning("Delays less than one tick are not possible, defaulting to one tick.");
			}
		}

		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event e) {
		debug(e, true);
		long start = Skript.debug() ? System.nanoTime() : 0;
		TriggerItem next = getNext();
		if (next != null && Skript.getInstance().isEnabled()) { // See https://github.com/SkriptLang/Skript/issues/3702
			delayed.add(e);

			Timespan duration = this.duration.getSingle(e);
			if (duration == null)
				return null;
			
			// Back up local variables
			Object localVars = Variables.removeLocals(e);
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
				if (Skript.debug())
					Skript.info(getIndentation() + "... continuing after " + (System.nanoTime() - start) / 1000000000. + "s");

				// Re-set local variables
				if (localVars != null)
					Variables.setLocalVariables(e, localVars);

				Object timing = null;
				if (SkriptTimings.enabled()) { // getTrigger call is not free, do it only if we must
					Trigger trigger = getTrigger();
					if (trigger != null) {
						timing = SkriptTimings.start(trigger.getDebugLabel());
					}
				}

				TriggerItem.walk(next, e);
				Variables.removeLocals(e); // Clean up local vars, we may be exiting now

				SkriptTimings.stop(timing); // Stop timing if it was even started
			}, Math.max(duration.getTicks_i(), 1)); // Minimum delay is one tick, less than it is useless!
		}
		return null;
	}

	@Override
	protected void execute(Event e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "wait for " + duration.toString(e, debug) + (e == null ? "" : "...");
	}

	private static final Set<Event> delayed = Collections.newSetFromMap(new WeakHashMap<>());

	public static boolean isDelayed(Event e) {
		return delayed.contains(e);
	}

	public static void addDelayedEvent(Event event){
		delayed.add(event);
	}

}
