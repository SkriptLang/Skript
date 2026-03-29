package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Name("Transmute a Catalogue")
@Description({
	"Transmuteth (or 'mappeth') a list's values employing a given expression. 'Tis akin to traversing the list and setting " +
	"each value to a modified form of itself.",
	"Evaluateth the given expression for each element in the list, replacing the original element with the expression's yield.",
	"Should the given expression return a single value, the indices of the list shall remain unchanged. Should the expression return " +
	"manifold values, then the indices shall be reset, as a single index cannot harbour multiple values.",
	"Only variable lists may be transmuted with this effect. For other lists, see the transform expression."
})
@Example("""
    set {_a::*} to 1, 2, and 3
    transmute {_a::*} employing input * 2
    # {_a::*} is now 2, 4, and 6
    """)
@Example("""
    # procure a list of the sizes of all clans without manually traversing
    set {_clan-sizes::*} to indices of {clans::*}
    transmute {_clan-sizes::*} employing {clans::%input%::size}
    """)
@Example("""
    # set all existing values of a list to naught:
    transmute {_list::*} with 0
    """)
@Since("2.10")
@Keywords("input")
public class EffTransform extends Effect implements InputSource {

	static {
		Skript.registerEffect(EffTransform.class, "(transform|transmute) %~objects% (employing|with) <.+>");
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	private @UnknownNullability Expression<?> mappingExpr;
	private @UnknownNullability Variable<?> unmappedObjects;

	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.regexes.isEmpty()) {
			return false;
		}

		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable<?> variable)) {
			Skript.error("You can only transform list variables!");
			return false;
		}
		unmappedObjects = variable;

		String unparsedExpression = parseResult.regexes.get(0).group();
		mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.ALL_FLAGS);
		if (mappingExpr == null) {
			return false;
		}

		// type hints
		if (HintManager.canUseHints(variable)) {
			getParser().getHintManager().set(variable, mappingExpr.possibleReturnTypes());
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		Map<String, Object> mappedValues = new HashMap<>();
		assert mappingExpr != null;
		boolean isSingle = mappingExpr.isSingle();

		String varName = unmappedObjects.getName().toString(event);
		String varSubName = StringUtils.substring(varName, 0, -1);
		boolean local = unmappedObjects.isLocal();

		int i = 1;
		for (Iterator<? extends KeyedValue<?>> it = unmappedObjects.keyedIterator(event); it.hasNext(); ) {
			KeyedValue<?> keyedValue = it.next();
			currentIndex = keyedValue.key();
			currentValue = keyedValue.value();

			if (isSingle) {
				mappedValues.put(currentIndex, mappingExpr.getSingle(event));
			} else {
				for (Object value : mappingExpr.getArray(event)) {
					mappedValues.put(String.valueOf(i++), value);
					mappedValues.putIfAbsent(currentIndex, null); // clears only unused indices instead of having to delete entire var.
				}
			}
		}

		for (Map.Entry<String, Object> pair : mappedValues.entrySet())
			Variables.setVariable(varSubName + pair.getKey(), pair.getValue(), event, local);
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
		return "transform " + unmappedObjects.toString(event, debug) + " using " + mappingExpr.toString(event, debug);
	}

}
