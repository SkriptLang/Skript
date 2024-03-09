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
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

@Name("Filter")
@Description({
	"Filters a list based on a condition. ",
	"For example, if you ran 'broadcast \"something\" and \"something else\" where [string input is \"something\"]', ",
	"only \"something\" would be broadcast as it is the only string that matched the condition."
})
@Examples("send \"congrats on being staff!\" to all players where [player input has permission \"staff\"]")
@Since("2.2-dev36")
@SuppressWarnings({"null", "unchecked"})
public class ExprFilter extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprFilter.class, Object.class, ExpressionType.COMBINED,
				"%objects% (where|that match) \\[<.+>\\]");
		ParserInstance.registerData(FilterData.class, FilterData::new);
	}

	private Condition filterCondition;
	private String unparsedCondition;
	private Expression<?> unfilteredObjects;
	private Set<ExprFilterInput<?>> dependentInputs = new HashSet<>();

	@Nullable
	private Object currentFilterValue;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		unfilteredObjects = LiteralUtils.defendExpression(exprs[0]);
		if (unfilteredObjects.isSingle() || !LiteralUtils.canInitSafely(unfilteredObjects))
			return false;
		unparsedCondition = parseResult.regexes.get(0).group();
		FilterData filterData = getParser().getData(FilterData.class);
		ExprFilter originalParentFilter = filterData.parentFilter;
		filterData.parentFilter = this;
		filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: " + unparsedCondition);
		filterData.parentFilter = originalParentFilter;
		return filterCondition != null;
	}

	@NonNull
	@Override
	public Iterator<?> iterator(Event event) {
		Iterator<?> unfilteredObjectIterator = unfilteredObjects.iterator(event);
		if (unfilteredObjectIterator == null)
			return Collections.emptyIterator();
		return Iterators.filter(unfilteredObjectIterator, candidateObject -> {
			currentFilterValue = candidateObject;
			return filterCondition.check(event);
		});
	}

	@Override
	protected Object[] get(Event event) {
		try {
			return Converters.convertStrictly(Iterators.toArray(iterator(event), Object.class), getReturnType());
		} catch (ClassCastException e1) {
			return null;
		}
	}

	@Override
	public Class<?> getReturnType() {
		return unfilteredObjects.getReturnType();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(Event event, boolean debug) {
		return unfilteredObjects.toString(event, debug) + " that match [" + unparsedCondition + "]";
	}

	private boolean matchesAnySpecifiedTypes(String candidateString) {
		for (ExprFilterInput<?> dependentInput : dependentInputs) {
			ClassInfo<?> specifiedType = dependentInput.getSpecifiedType();
			if (specifiedType == null)
				return false;
			Pattern[] specifiedTypePatterns = specifiedType.getUserInputPatterns();
			if (specifiedTypePatterns == null)
				return false;

			for (Pattern typePattern : specifiedTypePatterns) {
				if (typePattern.matcher(candidateString).matches()) {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public boolean isLoopOf(String candidateString) {
		return unfilteredObjects.isLoopOf(candidateString) || matchesAnySpecifiedTypes(candidateString);
	}

	public Set<ExprFilterInput<?>> getDependentInputs() {
		return dependentInputs;
	}

	@Nullable
	public Object getCurrentFilterValue() {
		return currentFilterValue;
	}

	public static class FilterData extends ParserInstance.Data {

		@Nullable
		private ExprFilter parentFilter;

		public FilterData(ParserInstance parserInstance) {
			super(parserInstance);
		}

		@Nullable
		public ExprFilter getParentFilter() {
			return parentFilter;
		}

	}

}
