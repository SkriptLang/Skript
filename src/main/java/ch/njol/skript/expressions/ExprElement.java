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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Iterator;

@Name("Elements")
@Description({
		"The first, last, range or a random element of a set, e.g. a list variable.",
		"See also: <a href='#ExprRandom'>random expression</a>"
})
@Examples({
	"broadcast the first 3 elements of {top players::*}",
	"set {_last} to last element of {top players::*}",
	"set {_random player} to random element out of all players",
	"send 2nd last element of {top players::*} to player",
	"set {page2::*} to elements from 11 to 20 of {top players::*}"
})
@Since("2.0, 2.7 (relative to last element), 2.8.0 (range of elements)")
public class ExprElement<T> extends SimpleExpression<T> {

	private static final Patterns<ElementType[]> PATTERNS = new Patterns<>(new Object[][]{
		{"[the] (first|1:last) element [out] of %objects%", new ElementType[] {ElementType.FIRST_ELEMENT, ElementType.LAST_ELEMENT}},
		{"[the] (first|1:last) %integer% elements [out] of %objects%", new ElementType[] {ElementType.FIRST_X_ELEMENTS, ElementType.LAST_X_ELEMENTS}},
		{"[a] random element [out] of %objects%", new ElementType[] {ElementType.RANDOM}},
		{"[the] %integer%(st|nd|rd|th) [1:[to] last] element [out] of %objects%", new ElementType[] {ElementType.ORDINAL, ElementType.TAIL_END_ORDINAL}},
		{"[the] elements (from|between) %integer% (to|and) %integer% [out] of %objects%", new ElementType[] {ElementType.RANGE}}
	});

	static {
		//noinspection unchecked
		Skript.registerExpression(ExprElement.class, Object.class, ExpressionType.PROPERTY, PATTERNS.getPatterns());
	}

	private enum ElementType {
		FIRST_ELEMENT,
		LAST_ELEMENT,
		FIRST_X_ELEMENTS,
		LAST_X_ELEMENTS,
		RANDOM,
		ORDINAL,
		TAIL_END_ORDINAL,
		RANGE
	}

	private Expression<? extends T> expr;
	private	@Nullable Expression<Integer> startIndex, endIndex;
	private ElementType type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ElementType[] types = PATTERNS.getInfo(matchedPattern);
		expr = LiteralUtils.defendExpression(exprs[exprs.length - 1]);
		switch (type = types[parseResult.mark]) {
			case RANGE:
				endIndex = (Expression<Integer>) exprs[1];
				//$FALL-THROUGH$
			case FIRST_X_ELEMENTS:
			case LAST_X_ELEMENTS:
			case ORDINAL:
			case TAIL_END_ORDINAL:
				startIndex = (Expression<Integer>) exprs[0];
				break;
			default:
				startIndex = null;
				break;
		}
		return LiteralUtils.canInitSafely(expr);
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	protected T[] get(Event event) {
		Iterator<? extends T> iterator = expr.iterator(event);
		if (iterator == null || !iterator.hasNext())
			return null;
		T element = null;
		Class<T> returnType = (Class<T>) getReturnType();
		int startIndex = 0, endIndex = 0;
		if (this.startIndex != null) {
			Integer integer = this.startIndex.getSingle(event);
			if (integer == null)
				return null;
			startIndex = integer;
			if (startIndex <= 0 && type != ElementType.RANGE)
				return null;
		}
		if (this.endIndex != null) {
			Integer integer = this.endIndex.getSingle(event);
			if (integer == null)
				return null;
			endIndex = integer;
		}
		T[] elementArray;
		switch (type) {
			case FIRST_ELEMENT:
				element = iterator.next();
				break;
			case LAST_ELEMENT:
				element = Iterators.getLast(iterator);
				break;
			case RANDOM:
				element = CollectionUtils.getRandom(Iterators.toArray(iterator, returnType));
				break;
			case ORDINAL:
				Iterators.advance(iterator, startIndex - 1);
				if (!iterator.hasNext())
					return null;
				element = iterator.next();
				break;
			case TAIL_END_ORDINAL:
				elementArray = Iterators.toArray(iterator, returnType);
				if (startIndex > elementArray.length)
					return null;
				element = elementArray[elementArray.length - startIndex];
				break;
			case FIRST_X_ELEMENTS:
				return Iterators.toArray(Iterators.limit(iterator, startIndex), returnType);
			case LAST_X_ELEMENTS:
				elementArray = Iterators.toArray(iterator, returnType);
				startIndex = Math.min(startIndex, elementArray.length);
				return CollectionUtils.subarray(elementArray, elementArray.length - startIndex, elementArray.length);
			case RANGE:
				elementArray = Iterators.toArray(iterator, returnType);
				boolean reverse = startIndex > endIndex;
				int from = Math.min(startIndex, endIndex) - 1;
				int to = Math.max(startIndex, endIndex);
				T[] elements = CollectionUtils.subarray(elementArray, from, to);
				if (reverse)
					ArrayUtils.reverse(elements);
				return elements;
		}
		//noinspection unchecked
		elementArray = (T[]) Array.newInstance(getReturnType(), 1);
		elementArray[0] = element;
		return elementArray;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		Expression<? extends R> convExpr = expr.getConvertedExpression(to);
		if (convExpr == null)
			return null;

		ExprElement<R> exprElement = new ExprElement<>();
		exprElement.expr = convExpr;
		exprElement.startIndex = startIndex;
		exprElement.endIndex = endIndex;
		exprElement.type = type;
		return exprElement;
	}

	@Override
	public boolean isSingle() {
		return type != ElementType.FIRST_X_ELEMENTS && type != ElementType.LAST_X_ELEMENTS && type != ElementType.RANGE;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return expr.getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String prefix;
		switch (type) {
			case FIRST_ELEMENT:
				prefix = "the first";
				break;
			case LAST_ELEMENT:
				prefix = "the last";
				break;
			case FIRST_X_ELEMENTS:
				assert startIndex != null;
				prefix = "the first " + startIndex.toString(event, debug);
				break;
			case LAST_X_ELEMENTS:
				assert startIndex != null;
				prefix = "the last " + startIndex.toString(event, debug);
				break;
			case RANDOM:
				prefix = "a random";
				break;
			case ORDINAL:
			case TAIL_END_ORDINAL:
				assert startIndex != null;
				prefix = "the ";
				// Proper ordinal number
				if (startIndex instanceof Literal) {
					Integer integer = ((Literal<Integer>) startIndex).getSingle();
					if (integer == null)
						prefix += startIndex.toString(event, debug) + "th";
					else
						prefix += StringUtils.fancyOrderNumber(integer);
				} else {
					prefix += startIndex.toString(event, debug) + "th";
				}
				if (type == ElementType.TAIL_END_ORDINAL)
					prefix += " last";
				break;
			case RANGE:
				assert startIndex != null && endIndex != null;
				return "the elements from " + startIndex.toString(event, debug) + " to " + endIndex.toString(event, debug) + " of " + expr.toString(event, debug);
			default:
				throw new IllegalStateException();
		}
		return prefix + (isSingle() ? " element" : " elements") + " of " + expr.toString(event, debug);
	}

}
