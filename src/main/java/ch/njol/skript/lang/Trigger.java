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
package ch.njol.skript.lang;

import java.io.File;
import java.util.List;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.variables.Variables;

/**
 * @author Peter Güttinger
 */
public class Trigger extends TriggerSection {
	
	private final String name;
	private final SkriptEvent event;
	
	@Nullable
	private final File script;
	private int line = -1; // -1 is default: it means there is no line number available
	private String debugLabel;
	
	public Trigger(final @Nullable File script, final String name, final SkriptEvent event, final List<TriggerItem> items) {
		super(items);
		this.script = script;
		this.name = name;
		this.event = event;
		this.debugLabel = "unknown trigger";
	}

	/**
	 * Executes this trigger for certain event.
	 * @param e Event.
	 * @param removeLocals Should local variables be deleted after execution.
	 * @return false if an exception occurred
	 */
	public boolean execute(final Event e, boolean removeLocals) {
		boolean success = TriggerItem.walk(this, e);
		// Clear local variables
		if(removeLocals)
			Variables.removeLocals(e);
		return success;
	}

	/**
	 * Executes this trigger for certain event.
	 * @param e Event.
	 * @return false if an exception occurred
	 */
	public boolean execute(final Event e) {
		return execute(e, true);
	}
	
	@Override
	@Nullable
	protected TriggerItem walk(final Event e) {
		return walk(e, true);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return name + " (" + event.toString(e, debug) + ")";
	}
	
	/**
	 * Gets name of this trigger.
	 * @return Name of trigger.
	 */
	public String getName() {
		return name;
	}
	
	public SkriptEvent getEvent() {
		return event;
	}
	
	@Nullable
	public File getScript() {
		return script;
	}

	/**
	 * Sets line number for this trigger's start.
	 * Only used for debugging.
	 * @param line Line number
	 */
	public void setLineNumber(int line) {
		this.line  = line;
	}
	
	/**
	 * Gets line number for this trigger's start.
	 * Only use it for debugging!
	 * @return Line number.
	 */
	public int getLineNumber() {
		return line;
	}
	
	public void setDebugLabel(String label) {
		this.debugLabel = label;
	}
	
	public String getDebugLabel() {
		return debugLabel;
	}
	
}
