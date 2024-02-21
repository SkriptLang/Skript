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
package org.skriptlang.skript.lang.debug;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public final class Debuggers {

	private Debuggers() {
		throw new AssertionError();
	}

	private static final List<Debugger> attachedDebuggers = new ArrayList<>();

	public static void onParse(SyntaxElement element) {
		if (attachedDebuggers.isEmpty())
			return;
		for (Debugger debugger : attachedDebuggers) {
			debugger.onParse(element);
		}
	}

	public static void onWalk(TriggerItem triggerItem, Event event) {
		if (attachedDebuggers.isEmpty())
			return;
		for (Debugger debugger : attachedDebuggers) {
			debugger.onWalk(triggerItem, event);
		}
	}

	public static boolean enabled() {
		return !attachedDebuggers.isEmpty();
	}

	/**
	 * Attaches a debugger
	 *
	 * @param debugger The debugger to attach
	 */
	public static void attachDebugger(Debugger debugger) {
		if (debugger == null)
			throw new SkriptAPIException("Cannot attach a debugger because it is null");
		attachedDebuggers.add(debugger);
	}

	/**
	 * Detaches a debugger
	 *
	 * @param debugger The debugger to detach
	 */
	public static void detachDebugger(Debugger debugger) {
		attachedDebuggers.remove(debugger);
	}

}
