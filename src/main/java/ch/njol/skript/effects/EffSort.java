package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.expressions.ExprSortedList;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

@Name("Sort")
@Description("""
	Sorts a list variable using either the natural ordering of the contents or the results of the given expression.
	Be warned, this will overwrite the indices of the list variable.
	
	When using the full <code>sort %~objects% (by|based on) &lt;expression&gt;</code> pattern,
	the input expression can be used to refer to the current item being sorted.
	(See input expression for more information.)""")
@Example("set {_words::*} to \"pineapple\", \"banana\", \"yoghurt\", and \"apple\"")
@Example("sort {_words::*} # alphabetical sort")
@Example("sort {_words::*} by length of input # shortest to longest")
@Example("sort {_words::*} in descending order by length of input # longest to shortest")
@Example("sort {_words::*} based on {tastiness::%input%} # sort based on custom value")
@Since("2.9.0, 2.10 (sort order)")
@Keywords("input")
public class EffSort extends Effect implements InputSource {

	private record MappedValue(Object original, Object mapped) { }

	static {
		Skript.registerEffect(EffSort.class, "sort %~objects% [in (:descending|ascending) order] [(by|based on) <.+>]");
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}


	private @Nullable Expression<?> mappingExpr;
	private @UnknownNullability Variable<?> unsortedObjects;
	private boolean descendingOrder;

	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable<?> variable)) {
			Skript.error("You can only sort list variables!");
			return false;
		}
		unsortedObjects = variable;
		descendingOrder = parseResult.hasTag("descending");

		//noinspection DuplicatedCode
		if (!parseResult.regexes.isEmpty()) {
			@Nullable String unparsedExpression = parseResult.regexes.get(0).group();
			assert unparsedExpression != null;
			mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.PARSE_EXPRESSIONS);
			if (mappingExpr == null)
				return false;
			if (!mappingExpr.isSingle()) {
				Skript.error("The mapping expression in the sort effect must only return a single value for a single input.");
				return false;
			}
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
			List<MappedValue> mappedValues = new ArrayList<>();
			for (Iterator<? extends KeyedValue<?>> it = unsortedObjects.keyedIterator(event); it.hasNext(); ) {
				KeyedValue<?> keyedValue = it.next();
				currentIndex = keyedValue.key();
				currentValue = keyedValue.value();
				Object mappedValue = mappingExpr.getSingle(event);
				if (mappedValue == null) {
					error("Sorting failed because Skript cannot sort null values. "
						+ "The mapping expression '" + mappingExpr.toString(event, false)
						+ "' returned a null value when given the input '"+currentValue+"'.");
					return;
				}
				mappedValues.add(new MappedValue(currentValue, mappedValue));
			}
			try {
				sorted = mappedValues.stream()
					.sorted((o1, o2) -> ExprSortedList.compare(o1.mapped(), o2.mapped()) * sortingMultiplier)
					.map(MappedValue::original)
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
