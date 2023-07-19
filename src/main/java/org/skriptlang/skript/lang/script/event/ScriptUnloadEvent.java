package org.skriptlang.skript.lang.script.event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.lang.script.Script;

/**
 * Called when a Script is unloaded in the {@link ScriptLoader}.
 * Note that this event triggers <b>before</b> the script is actually unloaded.
 *
 * @see ScriptLoader#unloadScript(Script)
 */
@FunctionalInterface
public interface ScriptUnloadEvent extends ScriptLoaderEvent, ScriptEvent {

	/**
	 * The method that is called when this event triggers.
	 *
	 * @param parser The ParserInstance handling the unloading of <code>script</code>.
	 * @param script The Script being unloaded.
	 */
	void onUnload(ParserInstance parser, Script script);

}
