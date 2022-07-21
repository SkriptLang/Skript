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
package ch.njol.skript.lang.script;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A ScriptEventHandler is used for listening to and performing actions for different Script events.
 */
public abstract class ScriptEventHandler {

	/**
	 * Called when this Script is loaded.
	 *
	 * @param oldScript The Script that was just unloaded.
	 *                  Null if there wasn't a Script unloaded.
	 */
	public void onLoad(@Nullable Script oldScript) { }

	/**
	 * Called when this Script is unloaded.
	 *
	 * @param newScript The Script that will be loaded after this one is unloaded.
	 *                  Null if there won't be a Script loaded.
	 */
	public void onUnload(@Nullable Script newScript) { }

}
