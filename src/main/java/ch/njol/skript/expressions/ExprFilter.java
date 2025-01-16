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
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Name("Filter")
@Description({
	"Filters a list based on a condition. ",
	"For example, if you ran 'broadcast \"something\" and \"something else\" where [string input is \"something\"]', ",
	"only \"something\" would be broadcast as it is the only string that matched the condition."
})
@Examples({
	"send \"congrats on being staff!\" to all players where [player input has permission \"staff\"]",
	"loop (all blocks in radius 5 of player) where [block input is not air]:"
})
@Since("2.2-dev36, 2.10 (parenthesis pattern)")
public class ExprFilter extends SimpleExpression<Object> implements InputSource {

	static {
		Skript.registerExpression(ExprFilter.class, Object.class, ExpressionType.COMBINED,
				"%objects% (where|that match) \\[<.+>\\]",
				"%objects% (where|that match) \\(<.+>\\)"
			);
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	private @UnknownNullability Condition filterCondition;
	private @UnknownNullability String unparsedCondition;
	private @UnknownNullability Expression<?> unfilteredObjects;
	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		unfilteredObjects = LiteralUtils.defendExpression(expressions[0]);
		if (unfilteredObjects.isSingle() || !LiteralUtils.canInitSafely(unfilteredObjects))
			return false;
		unparsedCondition = parseResult.regexes.get(0).group();
		InputData inputData = getParser().getData(InputData.class);
		InputSource originalSource = inputData.getSource();
		inputData.setSource(this);
		filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: " + unparsedCondition);
		inputData.setSource(originalSource);
		return filterCondition != null;
	}


	@Override
	public @NotNull Iterator<?> iterator(Event event) {
		if (unfilteredObjects instanceof Variable<?>) {
			Iterator<Pair<String, Object>> variableIterator = ((Variable<?>) unfilteredObjects).variablesIterator(event);
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(variableIterator, Spliterator.ORDERED), false)
				.filter(pair -> {
					currentValue = pair.getValue();
					currentIndex = pair.getKey();
					return filterCondition.check(event);
				})
				.map(Pair::getValue)
				.iterator();
		}

		// clear current index just to be safe
		currentIndex = null;

		Iterator<?> unfilteredObjectIterator = unfilteredObjects.iterator(event);
		if (unfilteredObjectIterator == null)
			return Collections.emptyIterator();
		return Iterators.filter(unfilteredObjectIterator, candidateObject -> {
			currentValue = candidateObject;
			return filterCondition.check(event);
		});
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		try {
			return Converters.convertStrictly(Iterators.toArray(iterator(event), Object.class), getReturnType());
		} catch (ClassCastException e1) {
			return null;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return unfilteredObjects.getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return unfilteredObjects.toString(event, debug) + " that match [" + unparsedCondition + "]";
	}

	private boolean matchesAnySpecifiedTypes(String candidateString) {
		for (ExprInput<?> dependentInput : dependentInputs) {
			ClassInfo<?> specifiedType = dependentInput.getSpecifiedType();
			if (specifiedType == null)
				return false;
			if (specifiedType.matchesUserInput(candidateString))
				return true;
		}
		return false;
	}

	@Override
	public boolean isLoopOf(String candidateString) {
		return unfilteredObjects.isLoopOf(candidateString) || matchesAnySpecifiedTypes(candidateString);
	}

	public Set<ExprInput<?>> getDependentInputs() {
		return dependentInputs;
	}

	public @Nullable Object getCurrentValue() {
		return currentValue;
	}

	@Override
	public boolean hasIndices() {
		return unfilteredObjects instanceof Variable<?>;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

}
