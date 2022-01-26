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

import java.lang.reflect.Array;
import java.util.Arrays;

import ch.njol.skript.util.LiteralUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Sorted List")
@Description({"Sorts given list in natural order. All objects in list must be comparable;",
	"if they're not, this expression will return nothing."
})
@Examples({"set {_sorted::*} to sorted {_players::*}"})
@Since("2.2-dev19")
public class ExprSortedList extends SimpleExpression<Object> {

	static{
		Skript.registerExpression(ExprSortedList.class, Object.class, ExpressionType.COMBINED, "sorted %objects%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> list;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(exprs[0]);
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	@Nullable
	protected Object[] get(Event e) {
		Object[] unsorted = list.getArray(e);
		Object[] sorted = (Object[]) Array.newInstance(getReturnType(), unsorted.length); // Not yet sorted...
		
		for (int i = 0; i < sorted.length; i++) {
			Object value = unsorted[i];
			if (value instanceof Long) {
				// Hope it fits to the double...
				sorted[i] = Double.valueOf(((Long) value).longValue());
			} else {
				// No conversion needed
				sorted[i] = value;
			}
		}
		
		try {
			Arrays.sort(sorted); // Now sorted
		} catch (IllegalArgumentException | ClassCastException ex) { // In case elements are not comparable
			return new Object[]{}; // We don't have a sorted array available
		}
		return sorted;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return list.getReturnType();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "sorted " + list.toString(e, debug);
	}

}
