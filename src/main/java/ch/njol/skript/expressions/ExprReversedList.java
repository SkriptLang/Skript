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
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;

@Name("Reversed List")
@Description("Reverses given list.")
@Examples({"set {_list::*} to reversed {_list::*}"})
@Since("2.4")
public class ExprReversedList extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprReversedList.class, Object.class, ExpressionType.COMBINED, "reversed %objects%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> list;

	@SuppressWarnings("unused")
	public ExprReversedList() {
	}

	public ExprReversedList(Expression<?> list) {
		this.list = list;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		list = LiteralUtils.defendExpression(exprs[0]);
		return LiteralUtils.canInitSafely(list);
	}

	@Override
	@Nullable
	protected Object[] get(Event e) {
		Object[] inputArray = list.getArray(e).clone();
		Object[] array = (Object[]) Array.newInstance(getReturnType(), inputArray.length);
		System.arraycopy(inputArray, 0, array, 0, inputArray.length);
		reverse(array);
		return array;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;

		Expression<? extends R> convertedList = list.getConvertedExpression(to);
		if (convertedList != null)
			return (Expression<? extends R>) new ExprReversedList(convertedList);

		return null;
	}

	private void reverse(Object[] array) {
		for (int i = 0; i < array.length / 2; i++) {
			Object temp = array[i];
			int reverse = array.length - i - 1;
			array[i] = array[reverse];
			array[reverse] = temp;
		}
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
		return "reversed " + list.toString(e, debug);
	}

}
