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
package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

@Name("While Loop")
@Description("While Loop sections are loops that will just keep repeating as long as a condition is met.")
@Examples({
	"while size of all players < 5:",
	"\tsend \"More players are needed to begin the adventure\" to all players",
	"\twait 5 seconds",
	"",
	"set {_counter} to 1",
	"do while {_counter} > 1: # false but will increase {_counter} by 1 then get out",
	"\tadd 1 to {_counter}",
	"",
	"# Be careful when using while loops with conditions that are almost ",
	"# always true for a long time without using 'wait %timespan%' inside it, ",
	"# otherwise it will probably hang and crash your server.",
	"while player is online:",
	"\tgive player 1 dirt",
	"\twait 1 second # without using a delay effect the server will crash",
})
@Since("2.0, 2.6 (do while)")
public class SecWhile extends Section {

	static {
		Skript.registerSection(SecWhile.class, "[(1¦do)] while <.+>");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Condition condition;

	@Nullable
	private TriggerItem actualNext;

	private boolean doWhile;
	private boolean ranDoWhile = false;

	@Override
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		String expr = parseResult.regexes.get(0).group();

		condition = Condition.parse(expr, "Can't understand this condition: " + expr);
		if (condition == null)
			return false;
		doWhile = parseResult.mark == 1;
		loadOptionalCode(sectionNode);

		super.setNext(this);

		return true;
	}

	@Nullable
	@Override
	protected TriggerItem walk(Event e) {
		if ((doWhile && !ranDoWhile) || condition.check(e)) {
			ranDoWhile = true;
			return walk(e, true);
		} else {
			reset();
			debug(e, false);
			return actualNext;
		}
	}

	@Override
	public SecWhile setNext(@Nullable TriggerItem next) {
		actualNext = next;
		return this;
	}

	@Nullable
	public TriggerItem getActualNext() {
		return actualNext;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (doWhile ? "do " : "") + "while " + condition.toString(e, debug);
	}

	public void reset() {
		ranDoWhile = false;
	}

}
