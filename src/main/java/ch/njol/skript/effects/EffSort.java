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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.expressions.ExprSortedList;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Name("Sort")
@Description({
	"Sorts a list variable using either the natural ordering of the contents or the results of the given expression.",
	"Be warned, this will overwrite the indices of the list variable."
})
@Examples({
	"set {_words::*} to \"pineapple\", \"banana\", \"yoghurt\", and \"apple\"",
	"sort {_words::*} # alphabetical sort",
	"sort {_words::*} by length of input # shortest to longest",
	"sort {_words::*} in descending order by length of input # longest to shortest",
	"sort {_words::*} based on {tastiness::%input%} # sort based on custom value"
})
@Since("2.9.0, INSERT VERSION (sort order)")
@Keywords("input")
public class EffSort extends Effect implements InputSource {

	static {
		Skript.registerEffect(EffSort.class, "sort %~objects% [in (:descending|ascending) order] [(by|based on) <.+>]");
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	@Nullable
	private Expression<?> mappingExpr;
	@Nullable
	private String unparsedExpression;
	private Variable<?> unsortedObjects;
	private boolean descendingOrder;

	private Set<ExprInput<?>> dependentInputs = new HashSet<>();

	@Nullable
	private Object currentValue;
	@UnknownNullability
	private String currentIndex;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable)) {
			Skript.error("You can only sort list variables!");
			return false;
		}
		unsortedObjects = (Variable<?>) expressions[0];
		descendingOrder = parseResult.hasTag("descending");

		if (!parseResult.regexes.isEmpty()) {
			unparsedExpression = parseResult.regexes.get(0).group();
			assert unparsedExpression != null;
			InputData inputData = getParser().getData(InputData.class);
			InputSource originalSource = inputData.getSource();
			inputData.setSource(this);
			mappingExpr = new SkriptParser(unparsedExpression, SkriptParser.PARSE_EXPRESSIONS, ParseContext.DEFAULT)
				.parseExpression(Object.class);
			inputData.setSource(originalSource);
			return mappingExpr != null && mappingExpr.isSingle();
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object[] sorted;
		int sortingMultiplier = descendingOrder ? -1 : 1;
		if (mappingExpr == null) {
			try {
				sorted = unsortedObjects.stream(event)
					.sorted((o1, o2) -> ExprSortedList.compare(o1, o2) * sortingMultiplier)
					.toArray();
			} catch (IllegalArgumentException | ClassCastException e) {
				return;
			}
		} else {
			Map<Object, Object> valueToMappedValue = new LinkedHashMap<>();
			for (Iterator<Pair<String, Object>> it = unsortedObjects.variablesIterator(event); it.hasNext(); ) {
				Pair<String, Object> pair = it.next();
				currentIndex = pair.getKey();
				currentValue = pair.getValue();
				Object mappedValue = mappingExpr.getSingle(event);
				if (mappedValue == null)
					return;
				valueToMappedValue.put(currentValue, mappedValue);
			}
			try {
				sorted = valueToMappedValue.entrySet().stream()
					.sorted(Map.Entry.comparingByValue((o1, o2) -> ExprSortedList.compare(o1, o2) * sortingMultiplier))
					.map(Map.Entry::getKey)
					.toArray();
			} catch (IllegalArgumentException | ClassCastException e) {
				return;
			}
		}

		unsortedObjects.change(event, sorted, ChangeMode.SET);
	}

	@Override
	public Set<ExprInput<?>> getDependentInputs() {
		return dependentInputs;
	}

	@Override
	public @Nullable Object getCurrentValue() {
		return currentValue;
	}

	@Override
	public boolean hasIndices() {
		return true;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "sort " + unsortedObjects.toString(event, debug)
				+ " in " + (descendingOrder ? "descending" : "ascending") + " order"
				+ (mappingExpr == null ? "" : " by " + mappingExpr.toString(event, debug));
	}

}
