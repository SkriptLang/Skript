package org.skriptlang.skript.lang.script;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.parser.ParserInstance;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A ScriptLoaderEvent is used for listening to and performing actions for different Script events on a global scale.
 * Note that some of these events may be used on a Script-specific scale, if they extend {@link ScriptEvent}.
 * @see ScriptLoader#registerEvent(ScriptLoaderEvent)
 */
public interface ScriptLoaderEvent {

	/**
	 * An event that is called when a Script is made active or inactive in a {@link ParserInstance}.
	 * Note that this event triggers <b>AFTER</b> the activity change has occurred.
	 * That is to say, the involved script will already have been made active/inactive when this event triggers.
	 * @see ParserInstance#isActive()
	 */
	@FunctionalInterface
	interface ScriptActivityChangeEvent extends ScriptLoaderEvent, ScriptEvent {

		/**
		 * Called when a Script is made active or inactive in a {@link ParserInstance}.
		 * Note that this event triggers <b>AFTER</b> the activity change has occurred.
		 * That is to say, the script will already have been made active/inactive when this event triggers.
		 *
		 * @param parser The ParserInstance where the activity change occurred.
		 * @param script The Script this event was registered for.
		 * @param active Whether <code>script</code> became active or inactive within <code>parser</code>.
		 * @param other The Script that was made active or inactive.
		 *  Whether it was made active or inactive is the negation of the <code>active</code>.
		 *  That is to say, if <code>script</code> became active, then <code>other</code> became inactive.
		 *  Null if <code>parser</code> was inactive (meaning no script became inactive)
		 *    or became inactive (meaning no script became active).
		 * @see ParserInstance#isActive()
		 */
		void onActivityChange(ParserInstance parser, Script script, boolean active, @Nullable Script other);

	}

	/**
	 * An event that is called right before a Script is unloaded in the {@link ScriptLoader}.
	 * Note that this event triggers <b>BEFORE</b> the script is actually unloaded.
	 * @see ScriptLoader#unloadScript(Script)
	 */
	@FunctionalInterface
	interface ScriptUnloadEvent extends ScriptLoaderEvent, ScriptEvent {

		/**
		 * Called when this Script is unloaded in the {@link ScriptLoader}.
		 * Note that this event triggers <b>BEFORE</b> the script is actually unloaded.
		 *
		 * @param parser The ParserInstance handling the unloading of <code>script</code>.
		 * @param script The Script being unloaded.
		 * @see ScriptLoader#unloadScript(Script)
		 */
		void onUnload(ParserInstance parser, Script script);

	}

}
