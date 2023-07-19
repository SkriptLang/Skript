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
 * Note that the script will contain {@link Structure}s that are not fully loaded.
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
