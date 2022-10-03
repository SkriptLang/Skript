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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.server.TabCompleteEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Tab Completion Arguments")
@Description("Get the arguments used within a tab complete event")
@Examples({"on tab complete of \"test\":",
	"\ttab argument 1 is \"bar\"",
	"\tset tab completions for position 2 to \"foo1\",\"foo2\", and \"foo3\""})
@Events("tab complete")
@Since("INSERT VERSION")
public class ExprTabCompletionArgs extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprTabCompletionArgs.class, String.class, ExpressionType.SIMPLE, "tab [complete] arg[ument](0¦s|1¦[(-| )]%-number%)");
	}

	private int pattern;
	@Nullable
	private Expression<Number> position;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(TabCompleteEvent.class)) {
			Skript.error("Tab completion arguments are only usable in a tab complete event.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}

		pattern = parseResult.mark;
		position = pattern != 1 ? null : (Expression<Number>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		TabCompleteEvent tabEvent = ((TabCompleteEvent) event);
		String buffer = tabEvent.getBuffer();
		String[] buffers = buffer.split(" ");

		if (pattern == 0) {
			String[] args = new String[buffers.length - 1];
			if (buffers.length - 1 >= 0)
				System.arraycopy(buffers, 1, args, 0, buffers.length - 1);
			return args;
		} else if (pattern == 1) {
			int position = this.position.getSingle(event).intValue();
			if (buffers.length >= position + 1) {
				return new String[]{buffers[position]};
			}
		}
		return new String[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	@Nullable
	public String toString(Event event, boolean debug) {
		String pos = pattern == 1 ? "-" + position.toString(event, debug) : "s";
		return "tab complete argument" + pos;
	}
}
