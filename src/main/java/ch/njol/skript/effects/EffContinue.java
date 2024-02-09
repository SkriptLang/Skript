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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

@Name("Continue")
@Description("Moves the loop to the next iteration. You may also continue an outer loop from an inner one." +
	" The loops are labelled from 1 until the current loop, starting with the outermost one.")
@Examples({
	"# Broadcast online moderators",
	"loop all players:",
		"\tif loop-value does not have permission \"moderator\":",
			"\t\tcontinue # filter out non moderators",
		"\tbroadcast \"%loop-player% is a moderator!\" # Only moderators get broadcast",
	" ",
	"# Game starting counter",
	"set {_counter} to 11",
	"while {_counter} > 0:",
		"\tremove 1 from {_counter}",
		"\twait a second",
		"\tif {_counter} != 1, 2, 3, 5 or 10:",
			"\t\tcontinue # only print when counter is 1, 2, 3, 5 or 10",
		"\tbroadcast \"Game starting in %{_counter}% second(s)\"",
})
@Since("2.2-dev37, 2.7 (while loops), 2.8.0 (outer loops)")
public class EffContinue extends Effect {

	static {
		Skript.registerEffect(EffContinue.class,
			"continue [this loop|[the] [current] loop]",
			"continue [the] %*integer%(st|nd|rd|th) loop"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private LoopSection loop;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private List<LoopSection> innerLoops;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		List<LoopSection> currentLoops = getParser().getCurrentSections(LoopSection.class);

		int size = currentLoops.size();
		if (size == 0) {
			Skript.error("The 'continue' effect may only be used in loops");
			return false;
		}

		int level = matchedPattern == 0 ? size : ((Literal<Integer>) exprs[0]).getSingle();
		if (level < 1) {
			Skript.error("Can't continue the " + StringUtils.fancyOrderNumber(level) + " loop");
			return false;
		}
		if (level > size) {
			Skript.error("Can't continue the " + StringUtils.fancyOrderNumber(level) + " loop as there " +
				(size == 1 ? "is only 1 loop" : "are only " + size + " loops") + " present");
			return false;
		}

		loop = currentLoops.get(level - 1);
		innerLoops = currentLoops.subList(level, size);
		return true;
	}

	@Override
	protected void execute(Event event) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		for (LoopSection loop : innerLoops)
			loop.exit(event);
		return loop;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "continue";
	}

}
