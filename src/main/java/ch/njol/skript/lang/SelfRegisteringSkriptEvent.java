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

import ch.njol.skript.config.Config;
import org.bukkit.event.Event;

import java.util.Objects;

/**
 * @deprecated Regular {@link org.skriptlang.skript.lang.structure.Structure} methods should be used.
 * See individual methods for their equivalents.
 */
@Deprecated
public abstract class SelfRegisteringSkriptEvent extends SkriptEvent {

	/**
	 * This method is called after the whole trigger is loaded for events that fire themselves.
	 *
	 * @param t the trigger to register to this event
	 * @deprecated This method's functionality can be replaced by overriding {@link #postLoad()}.
	 * Normally, that method would register the parsed trigger with {@link ch.njol.skript.SkriptEventHandler}.
	 * A reference to the {@link Trigger} is available through {@link #trigger}.
	 */
	public abstract void register(Trigger t);

	/**
	 * This method is called to unregister this event registered through {@link #register(Trigger)}.
	 *
	 * @param t the same trigger which was registered for this event
	 * @deprecated This method's functionality can be replaced by overriding {@link #unload()}.
	 * Normally, that method would unregister the parsed trigger with {@link ch.njol.skript.SkriptEventHandler}.
	 * A reference to the {@link Trigger} is available through {@link #trigger}.
	 */
	public abstract void unregister(Trigger t);

	/**
	 * This method is called to unregister all events registered through {@link #register(Trigger)}.
	 * This is called on all registered events, thus it can also only unregister the
	 * event it is called on.
	 * @deprecated This method should no longer be used.
	 * Each trigger should be unregistered through {@link #unregister(Trigger)}.
	 */
	public abstract void unregisterAll();

	@Override
	public boolean load() {
		boolean load = super.load();
		if (load)
			afterParse(Objects.requireNonNull(getParser().getCurrentScript()).getConfig());
		return load;
	}

	@Override
	public boolean postLoad() {
		register(trigger);
		return true;
	}

	@Override
	public void unload() {
		unregister(trigger);
	}

	@Override
	public final boolean check(Event e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is called when this event is parsed. Overriding this is
	 * optional, and usually not needed.
	 * @param config Script that is being parsed
	 * @deprecated Use {@link #postLoad()} instead.
	 */
	@Deprecated
	public void afterParse(Config config) {

	}

	@Override
	public boolean isEventPrioritySupported() {
		return false;
	}

}
