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
 * Copyright Peter G??ttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.LongStream;
import java.util.stream.Stream;


@Name("List Index")
@Description("Returns the index of an object from a list.")
@Examples({
	"command /listindex:",
	"\ttrigger:",
	"\t\tset {_list::*} to 1,2,3,4,5,1",
	"\t\tsend \"%first index of 1 in %{_list::*}%%\"",
	"\t\tsend \"%last index of 1 in %{_list::*}%%\"",
	"\t\tsend \"%all indexes of 1 in %{_list::*}%%\""
})
public class ExprListIndex extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprListIndex.class, Object.class, ExpressionType.COMBINED,
			"[(first|2??last)] [(1??variable)] index of %object% in %objects%",
			"[all] [(1??variable)] ind(ex|ic)es of %object% in %objects%");
	}

	private Expression<?> elementExpr, listExpr;

	private boolean variable, all, first;


	@Override
	public boolean init(Expression<?> [] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		variable = (parseResult.mark & 1) == 1;
		all = matchedPattern == 1;
		first = (parseResult.mark & 2) == 0;
		elementExpr = LiteralUtils.defendExpression(exprs[0]);
		listExpr = LiteralUtils.defendExpression(exprs[1]);
		if (listExpr.isSingle())
			return false;
		if (variable && !(listExpr instanceof Variable)) {
			Skript.error(listExpr + " is not a list variable");
			return false;
		}
		return LiteralUtils.canInitSafely(elementExpr, listExpr);
	}

	@Override
	protected @Nullable
	Object[] get(Event e) {
		Object element = elementExpr.getSingle(e);
		if (element == null) return null;
		if (variable) {
			Object o = ((Variable<?>) listExpr).getRaw(e);
			if (o instanceof TreeMap<?, ?>) {
				TreeMap<?, ?> listVar = new TreeMap<>();
				@SuppressWarnings("unchecked")
				Stream<String> lazyIndexes = ((TreeMap<String, ?>) (first ? listVar : listVar.descendingMap())).entrySet().stream()
					.filter(entry -> entry.getValue().equals(element)).map(Map.Entry::getKey);
				return all ? lazyIndexes.toArray(String[]::new) : lazyIndexes.findFirst().map(CollectionUtils::array).orElse(null);
			} else return null;
		} else {
			Object[] array = listExpr.getArray(e);
			if (all)
				return LongStream.range(0, array.length).filter(i -> element.equals(array[(int) i + 1])).boxed().toArray(Long[]::new);
			int index = first ? CollectionUtils.indexOf(array, element) : CollectionUtils.lastIndexOf(array, element);
			return index < 0 ? null : CollectionUtils.array(index + 1);
		}
	}

	@Override
	public boolean isSingle() {
		return !all;
	}

	@Override
	public Class<?> getReturnType() {
		return variable ? String.class : Long.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (all ? "all" : first ? "first" : "last") + (variable ? " variable" : "") + (isSingle() ? " index" : " indexes") + " of " + elementExpr.toString(e, debug) + " in " + listExpr.toString(e, debug);
	}
}
