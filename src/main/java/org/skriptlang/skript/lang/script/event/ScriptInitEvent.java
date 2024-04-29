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
package org.skriptlang.skript.lang.script.event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.OpenCloseable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import java.io.File;
import java.util.Set;

/**
 * Called when a {@link Script} is created and preloaded in the {@link ScriptLoader}.
 * The initializing script will contain {@link Structure}s that are not fully loaded.
 *
 * @see ScriptLoader#loadScripts(File, OpenCloseable)
 * @see ScriptLoader#loadScripts(Set, OpenCloseable)
 */
@FunctionalInterface
public interface ScriptInitEvent extends ScriptLoaderEvent {

	/**
	 * The method that is called when this event triggers.
	 *
	 * @param script The Script being initialized.
	 */
	void onInit(Script script);

}
