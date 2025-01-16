package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Name("Transform List")
@Description({
	"Transforms (or 'maps') a list's values using a given expression. This is akin to looping over the list and setting " +
	"each value to a modified version of itself.",
	"Evaluates the given expression for each element in the list, replacing the original element with the expression's result.",
	"If the given expression returns a single value, the indices of the list will not change. If the expression returns " +
	"multiple values, then then indices will be reset as a single index cannot contain multiple values.",
	"Only variable lists can be transformed with this effect. For other lists, see the transform expression."
})
@Examples({
	"set {_a::*} to 1, 2, and 3",
	"transform {_a::*} using input * 2",
	"# {_a::*} is now 2, 4, and 6",
	"",
	"# get a list of the sizes of all clans without manually looping",
	"set {_clan-sizes::*} to indices of {clans::*}",
	"transform {_clan-sizes::*} using {clans::%input%::size}",
	"",
	"# set all existing values of a list to 0:",
	"transform {_list::*} to 0"
})
@Since("2.10")
@Keywords("input")
public class EffTransform extends Effect implements InputSource {

	static {
		Skript.registerEffect(EffTransform.class, "(transform|map) %~objects% (using|with) <.+>");
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
		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable)) {
			Skript.error("You can only transform list variables!");
			return false;
		}
		unmappedObjects = (Variable<?>) expressions[0];

		//noinspection DuplicatedCode
		if (!parseResult.regexes.isEmpty()) {
			@Nullable String unparsedExpression = parseResult.regexes.get(0).group();
			assert unparsedExpression != null;
			mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.ALL_FLAGS);
			return mappingExpr != null;
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
		for (Iterator<Pair<String, Object>> it = unmappedObjects.variablesIterator(event); it.hasNext(); ) {
			Pair<String, Object> pair = it.next();
			currentIndex = pair.getKey();
			currentValue = pair.getValue();

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
