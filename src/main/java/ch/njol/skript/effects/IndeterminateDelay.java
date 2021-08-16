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

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;

/**
 * @author Peter Güttinger
 */
public class IndeterminateDelay extends Delay {
	
	@Override
	@Nullable
	protected TriggerItem walk(final Event e) {
		debug(e, true);
		final long start = Skript.debug() ? System.nanoTime() : 0;
		final TriggerItem next = getNext();
		if (next != null && Skript.getInstance().isEnabled()) { // See https://github.com/SkriptLang/Skript/issues/3702
			delayed.add(e);
			final Timespan d = duration.getSingle(e);
			if (d == null)
				return null;
			
			// Back up local variables
			Object localVars = Variables.removeLocals(e);
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (Skript.debug())
						Skript.info(getIndentation() + "... continuing after " + (System.nanoTime() - start) / 1000000000. + "s");
					
					// Re-set local variables
					if (localVars != null)
						Variables.setLocalVariables(e, localVars);
					
					TriggerItem.walk(next, e);
				}
			}, d.getTicks_i());
		}
		return null;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "wait for operation to finish";
	}
	
}
