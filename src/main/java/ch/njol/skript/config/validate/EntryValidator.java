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
package ch.njol.skript.config.validate;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Setter;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

/**
 * @author Peter Güttinger
 */
public class EntryValidator implements NodeValidator {
	
	@Nullable
	private final Consumer<String> setter;
	
	public EntryValidator() {
		setter = null;
	}

	// TODO Remove when Setter gets removed
	@Deprecated
	@ApiStatus.ScheduledForRemoval
	public EntryValidator(Setter<String> setter) {
		this((Consumer<String>) setter::set);
	}

	public EntryValidator(Consumer<String> setter) {
		this.setter = setter;
	}
	
	@Override
	public boolean validate(final Node node) {
		if (!(node instanceof EntryNode)) {
			notAnEntryError(node);
			return false;
		}
		if (setter != null)
			setter.accept(((EntryNode) node).getValue());
		return true;
	}
	
	public static void notAnEntryError(final Node node) {
		notAnEntryError(node, node.getConfig().getSeparator());
	}

	public static void notAnEntryError(final Node node, String separator) {
		SkriptLogger.setNode(node);
		Skript.error("'" + node.getKey() + "' is not an entry (like 'name " + separator + " value')");
	}
	
}
