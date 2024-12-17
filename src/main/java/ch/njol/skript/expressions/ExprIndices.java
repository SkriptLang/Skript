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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Map.Entry;

@Name("Indices of List")
@Description({
	"Returns all the indices of a list variable, optionally sorted by their values.",
	"To sort the indices, all objects in the list must be comparable;",
	"Otherwise, this expression will just return the unsorted indices."
})
@Examples({"set {l::*} to \"some\", \"cool\" and \"values\"",
		"broadcast \"%indices of {l::*}%\" # result is 1, 2 and 3", "",
		"set {_leader-board::first} to 17",
		"set {_leader-board::third} to 30",
		"set {_leader-board::second} to 25",
		"set {_leader-board::fourth} to 42",
		"set {_ascending-indices::*} to sorted indices of {_leader-board::*} in ascending order",
		"broadcast \"%{_ascending-indices::*}%\" #result is first, second, third, fourth",
		"set {_descending-indices::*} to sorted indices of {_leader-board::*} in descending order",
		"broadcast \"%{_descending-indices::*}%\" #result is fourth, third, second, first"
})
@Since("2.4 (indices), 2.6.1 (sorting)")
public class ExprIndices extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprIndices.class, String.class, ExpressionType.COMBINED,
				"[(the|all [[of] the])] (indexes|indices) of %~objects%",
				"%~objects%'[s] (indexes|indices)",
				"[sorted] (indices|indexes) of %~objects% in (ascending|1¦descending) order",
				"[sorted] %~objects%'[s] (indices|indexes) in (ascending|1¦descending) order"
		);
	}

	@SuppressWarnings({"null", "NotNullFieldNotInitialized"})
	private Variable<?> list;

	private boolean sort;
	private boolean descending;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sort = matchedPattern > 1;
		descending = parseResult.mark == 1;
		if (exprs[0] instanceof Variable<?> && ((Variable<?>) exprs[0]).isList()) {
			list = (Variable<?>) exprs[0];
			return true;
		}

		// things like "all indices of fake expression" shouldn't have any output at all
		if (LiteralUtils.canInitSafely(exprs[0])) {
			Skript.error("The indices expression may only be used with list variables");
		}

		return false;
	}

	@Nullable
	@Override
	@SuppressWarnings({"unchecked", "ConstantConditions"})
	protected String[] get(Event e) {
		Map<String, Object> variable = (Map<String, Object>) list.getRaw(e);

		if (variable == null) {
			return null;
		}

		if (sort) {
			int direction = descending ? -1 : 1;
			return variable.entrySet().stream()
				.map((entry) -> new Pair<>(
					entry.getKey(),
					entry.getValue() instanceof Map<?,?>
						? ((Map<?,?>) entry.getValue()).get(null)
						: entry.getValue()
				))
				.sorted((a, b) -> ExprSortedList.compare(a.getValue(), b.getValue()) * direction)
				.map(Pair::getKey)
				.toArray(String[]::new);
		}

		return variable.keySet().toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String text = "indices of " + list.toString(e, debug);

		if (sort)
			text = "sorted " + text + " in " + (descending ? "descending" : "ascending") + " order";

		return text;
	}

}
