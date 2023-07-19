package org.skriptlang.skript.lang.script.event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Config;
import ch.njol.util.OpenCloseable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * Called when {@link ScriptLoader} is preparing to load {@link Config}s into {@link Script}s.
 *
 * @see ScriptLoader#loadScripts(File, OpenCloseable)
 * @see ScriptLoader#loadScripts(Set, OpenCloseable)
 */
@FunctionalInterface
public interface PreScriptInitEvent extends ScriptLoaderEvent {

	/**
	 * The method that is called when this event triggers.
	 * Modifications to the given collection will affect what is loaded.
	 *
	 * @param configs The Configs to be loaded.
	 */
	void onPreInit(Collection<Config> configs);

}
