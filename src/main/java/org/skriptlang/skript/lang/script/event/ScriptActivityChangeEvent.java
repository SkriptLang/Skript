package org.skriptlang.skript.lang.script.event;

import ch.njol.skript.lang.parser.ParserInstance;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.script.Script;

/**
 * Called when a {@link Script} is made active or inactive in a {@link ParserInstance}.
 * Note that this event triggers <b>after</b> the change in activity has occurred.
 *
 * @see ParserInstance#isActive()
 */
@FunctionalInterface
public interface ScriptActivityChangeEvent extends ScriptLoaderEvent, ScriptEvent {

	/**
	 * The method that is called when this event triggers.
	 *
	 * @param parser The ParserInstance where the activity change occurred.
	 * @param script The Script this event was registered for.
	 * @param active Whether <code>script</code> became active or inactive within <code>parser</code>.
	 * @param other  The Script that was made active or inactive.
	 *               Whether it was made active or inactive is the negation of the <code>active</code>.
	 *               That is to say, if <code>script</code> became active, then <code>other</code> became inactive.
	 *               Null if <code>parser</code> was inactive (meaning no script became inactive)
	 *               or became inactive (meaning no script became active).
	 */
	void onActivityChange(ParserInstance parser, Script script, boolean active, @Nullable Script other);

}
