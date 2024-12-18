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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Name("Sort")
@Description("""
	Sorts a list variable using either the natural ordering of the contents or the results of the given expression.
	Be warned, this will overwrite the indices of the list variable.
	
	When using the full <code>sort %~objects% (by|based on) &lt;expression&gt;</code> pattern,
	the input expression can be used to refer to the current item being sorted.
	(See input expression for more information.)""")
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


	private @Nullable Expression<?> mappingExpr;
	private @UnknownNullability Variable<?> unsortedObjects;
	private boolean descendingOrder;

	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable)) {
			Skript.error("You can only sort list variables!");
			return false;
		}
		unsortedObjects = (Variable<?>) expressions[0];
		descendingOrder = parseResult.hasTag("descending");

		//noinspection DuplicatedCode
		if (!parseResult.regexes.isEmpty()) {
			@Nullable String unparsedExpression = parseResult.regexes.get(0).group();
			assert unparsedExpression != null;
			mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.PARSE_EXPRESSIONS);
			return mappingExpr != null;
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
