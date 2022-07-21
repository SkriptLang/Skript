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
