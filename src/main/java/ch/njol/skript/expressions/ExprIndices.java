package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Name("Indices of List")
@Description({
	"Returns all the indices of a list variable, optionally sorted by their values.",
	"To sort the indices, all objects in the list must be comparable;",
	"Otherwise, this expression will just return the unsorted indices."
})
@Example("""
		set {l::*} to "some", "cool" and "values"
		broadcast "%indices of {l::*}%" # result is 1, 2 and 3", "
		set {_leader-board::first} to 17
		set {_leader-board::third} to 30
		set {_leader-board::second} to 25
		set {_leader-board::fourth} to 42
		set {_ascending-indices::*} to sorted indices of {_leader-board::*} in ascending order
		broadcast "%{_ascending-indices::*}%" #result is first, second, third, fourth
		set {_descending-indices::*} to sorted indices of {_leader-board::*} in descending order
		broadcast "%{_descending-indices::*}%" #result is fourth, third, second, first
		""")
@Since("2.4 (indices), 2.6.1 (sorting)")
public class ExprIndices extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprIndices.class, String.class, ExpressionType.COMBINED,
				"[(the|all [[of] the])] (indexes|indices) of %~objects%",
				"%~objects%'[s] (indexes|indices)",
				"[sorted] (indices|indexes) of %~objects% in (ascending|1¦descending) order",
				"[sorted] %~objects%'[s] (indices|indexes) in (ascending|1¦descending) order"
		);
	}

	private KeyProviderExpression<?> keyedExpression;

	private boolean sort;
	private boolean descending;
	private boolean recursive;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		sort = matchedPattern > 1;
		descending = parseResult.mark == 1;

		Expression<?> expression = LiteralUtils.defendExpression(exprs[0]);

		if (!KeyProviderExpression.canReturnKeys(expression)) {
			Skript.error("The indices expression may only be used with keyed expressions");
			return false;
		}

		keyedExpression = (KeyProviderExpression<?>) exprs[0];
		recursive = exprs[0].returnsNestedStructures();
		if (!sort)
			exprs[0].returnNestedStructures(true); // force nested structures to be returned
		return true;

	}

	@Override
	protected String @Nullable [] get(Event event) {
		Object[] values = keyedExpression.getArray(event);
		String[] keys = keyedExpression.getArrayKeys(event);
		if (sort) {
			int direction = descending ? -1 : 1;
			return Arrays.stream(KeyedValue.zip(values, keys))
				.sorted((a, b) -> ExprSortedList.compare(a.value(), b.value()) * direction)
				.map(KeyedValue::key)
				.toArray(String[]::new);
		}

		if (recursive)
			return keys;

		for (int i = 0; i < keys.length; i++) {
			int separator = keys[i].indexOf(Variable.SEPARATOR);
			if (separator != -1)
				keys[i] = keys[i].substring(0, keys[i].indexOf(Variable.SEPARATOR));
		}
		return Arrays.stream(keys).distinct().toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean returnNestedStructures(boolean nested) {
		return keyedExpression.returnNestedStructures(nested);
	}

	@Override
	public boolean returnsNestedStructures() {
		return keyedExpression.returnsNestedStructures();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String text = "indices of " + keyedExpression.toString(event, debug);

		if (sort)
			text = "sorted " + text + " in " + (descending ? "descending" : "ascending") + " order";

		return text;
	}

}
