package org.skriptlang.skript.lang.script.event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.OpenCloseable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import java.io.File;
import java.util.Set;

/**
 * Called when a {@link Script} is loaded in the {@link ScriptLoader}.
 * Note that this event triggers <b>after</b> the script is completely loaded ({@link Structure} initialization finished).
 *
 * @see ScriptLoader#loadScripts(File, OpenCloseable)
 * @see ScriptLoader#loadScripts(Set, OpenCloseable)
 */
@FunctionalInterface
public interface ScriptLoadEvent extends ScriptLoaderEvent, ScriptEvent {

	/**
	 * The method that is called when this event triggers.
	 *
	 * @param parser The ParserInstance handling the loading of <code>script</code>.
	 * @param script The Script being loaded.
	 */
	void onLoad(ParserInstance parser, Script script);

}
