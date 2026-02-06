package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

@Name("Reduce")
@Description({
	"Reduces lists to single values by repeatedly applying an operation.",
	"The reduce expression takes each element and combines it with an accumulator value.",
	"Use 'reduced value' to access the current accumulated value and 'input' for the current element.",
})
@Example("set {_sum} to {_numbers::*} reduced with [reduced value + input]")
@Example("set {_product} to {_values::*} reduced with [reduced value * input]")
@Example("set {_concatenated} to {_strings::*} reduced with [\"%reduced value%%input%\"]")
@Since("INSERT VERSION")
@Keywords({"input", "reduced value", "accumulator"})
public class ExprReduce extends SimpleExpression<Object> implements InputSource {

	static {
		Skript.registerExpression(ExprReduce.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
			"%objects% (reduced|folded) (to|with|by) \\[<.+>\\]",
			"%objects% (reduced|folded) (to|with|by) \\(<.+>\\)"
		);
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	private boolean keyed;
	private Expression<?> reduceExpr;
	private Expression<?> unreducedObjects;

	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @Nullable Object reducedValue;
	private @UnknownNullability String currentIndex;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		unreducedObjects = LiteralUtils.defendExpression(expressions[0]);
		if (unreducedObjects.isSingle()) {
			Skript.error("A single value cannot be reduced. Only lists can be reduced.");
			return false;
		}
		if ((!LiteralUtils.canInitSafely(unreducedObjects)) || parseResult.regexes.isEmpty()) {
			return false;
		}

		keyed = KeyProviderExpression.canReturnKeys(unreducedObjects);

		@Nullable String unparsedExpression = parseResult.regexes.getFirst().group();
		assert unparsedExpression != null;
		reduceExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.ALL_FLAGS);

		return reduceExpr != null;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		try {
			boolean hadNullResult = false;

			if (keyed) {
				Iterator<? extends KeyedValue<?>> keyedIterator = ((KeyProviderExpression<?>) unreducedObjects).keyedIterator(event);
				if (keyedIterator == null || !keyedIterator.hasNext())
					return new Object[0];

				KeyedValue<?> first = keyedIterator.next();
				reducedValue = first.value();
				currentIndex = first.key();

				while (keyedIterator.hasNext()) {
					KeyedValue<?> next = keyedIterator.next();
					currentValue = next.value();
					currentIndex = next.key();

					Object result = reduceExpr.getSingle(event);
					if (result != null) {
						reducedValue = result;
					} else {
						hadNullResult = true;
					}
				}
			} else {
				currentIndex = null;

				Iterator<?> iterator = unreducedObjects.iterator(event);
				if (iterator == null || !iterator.hasNext())
					return null;

				reducedValue = iterator.next();

				int index = 1;
				while (iterator.hasNext()) {
					currentValue = iterator.next();
					currentIndex = String.valueOf(index);

					Object result = reduceExpr.getSingle(event);
					if (result != null) {
						reducedValue = result;
					} else {
						hadNullResult = true;
					}

					index++;
				}
			}

			if (hadNullResult) {
				warning("The reduce expression returned null for one or more elements, which were skipped.");
			}

			return new Object[] { reducedValue };
		} finally {
			currentValue = null;
			reducedValue = null;
			currentIndex = null;
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return reduceExpr.getReturnType();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return reduceExpr.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		return reduceExpr.canReturn(returnType);
	}

	@Override
	public Set<ExprInput<?>> getDependentInputs() {
		return dependentInputs;
	}

	@Override
	public @Nullable Object getCurrentValue() {
		return currentValue;
	}

	/**
	 * Gets the current reduced/accumulated value.
	 * This is accessible via ExprReducedValue.
	 */
	public @Nullable Object getReducedValue() {
		return reducedValue;
	}

	@Override
	public boolean hasIndices() {
		return keyed;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return unreducedObjects.toString(event, debug) + " reduced with [" + reduceExpr.toString(event, debug) + "]";
	}

}
