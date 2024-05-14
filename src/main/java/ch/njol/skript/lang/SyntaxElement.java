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
package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.Experimented;
import org.skriptlang.skript.lang.script.Script;

/**
 * Represents a general part of the syntax.
 */
public interface SyntaxElement extends Experimented {

	/**
	 * Called just after the constructor.
	 * 
	 * @param expressions all %expr%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out, it will still be included in this list
	 *            holding the default value of the desired type, which usually depends on the event.
	 * @param matchedPattern The index of the pattern which matched
	 * @param isDelayed Whether this expression is used after a delay or not (i.e. if the event has already passed when this expression will be called)
	 * @param parseResult Additional information about the match.
	 * @return Whether this expression was initialised successfully. An error should be printed prior to returning false to specify the cause.
	 * @see ParserInstance#isCurrentEvent(Class...)
	 */
	boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult);

	/**
	 * @see ParserInstance#get()
	 */
	default ParserInstance getParser() {
		return ParserInstance.get();
	}

	/**
	 * Available during parsing to check whether a script has an experiment enabled.
	 * @param experiment The experiment to test.
	 * @return Whether the currently-parsing script
	 * @throws SkriptAPIException If the current script is not available in the parser instance.
	 */
	@Override
	default boolean hasExperiment(Experiment experiment) throws SkriptAPIException {
		if (!this.getParser().isActive())
			return false;
		Script script = this.getParser().getCurrentScript();
		return script.hasExperiment(experiment);
	}

	/**
	 * Whether this syntax element consumes annotations.
	 * Consuming means the annotations are discarded for the following syntax.
	 * <p>
	 * <b>Most syntax should leave this as 'true'.</b>
	 * <p>
	 * If the return value is true (as expected), annotations placed before this element WILL NOT be available to
	 * the lines (or statements) following it.
	 *
	 * <pre>{@code
	 * 	on event:
	 *      @annotation
	 * 		my effect # can see @annotation
	 * 		my effect # cannot see @annotation
	 * }</pre>
	 *
	 * If the return value is false, annotations placed before this element WILL be available to
	 * the lines (or statements) following it.
	 *
	 * <pre>{@code
	 * 	on event:
	 *      @annotation
	 * 		my effect # can see @annotation
	 * 		my effect # can see @annotation
	 * }</pre>
	 *
	 * This behaviour is used by meta-syntax (including @annotations themselves).
	 *
	 * @return True if annotations will be discarded, false if they should be kept for the next statement.
	 * @see org.skriptlang.skript.lang.script.Annotation
	 */
	default boolean consumeAnnotations() {
		return true;
	}

}
