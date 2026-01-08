package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.KeyedValue.UnzippedKeyValues;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.*;
import java.util.stream.StreamSupport;

@Name("Filter")
@Description({
	"Filters a list based on a condition. ",
	"For example, if you ran 'broadcast \"something\" and \"something else\" where [string input is \"something\"]', ",
	"only \"something\" would be broadcast as it is the only string that matched the condition."
})
@Example("send \"congrats on being staff!\" to all players where [player input has permission \"staff\"]")
@Example("loop (all blocks in radius 5 of player) where [block input is not air]:")
@Since("2.2-dev36, 2.10 (parenthesis pattern)")
public class ExprFilter extends SimpleExpression<Object> implements InputSource, KeyProviderExpression<Object> {

	static {
		Skript.registerExpression(ExprFilter.class, Object.class, ExpressionType.COMBINED,
				"%objects% (where|that match) \\[<.+>\\]",
				"%objects% (where|that match) \\(<.+>\\)"
			);
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	private final Map<Event, List<String>> cache = new WeakHashMap<>();

	private boolean keyed;
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
		keyed = KeyProviderExpression.canReturnKeys(unfilteredObjects);
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
		if (keyed)
			return Iterators.transform(keyedIterator(event), KeyedValue::value);

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
	public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
		//noinspection unchecked
		Iterator<KeyedValue<Object>> keyedIterator = ((KeyProviderExpression<Object>) unfilteredObjects).keyedIterator(event);
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(keyedIterator, Spliterator.ORDERED), false)
			.filter(keyedValue -> {
				currentValue = keyedValue.value();
				currentIndex = keyedValue.key();
				return filterCondition.check(event);
			})
			.iterator();
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (!keyed)
			return Converters.convertStrictly(Iterators.toArray(iterator(event), Object.class), getReturnType());
		UnzippedKeyValues<Object> unzipped = KeyedValue.unzip(keyedIterator(event));
		cache.put(event, unzipped.keys());
		return Converters.convertStrictly(unzipped.values().toArray(), getReturnType());
	}

	@Override
	public @NotNull String @NotNull [] getArrayKeys(Event event) throws IllegalStateException {
		if (!cache.containsKey(event))
			throw new IllegalStateException();
		return cache.remove(event).toArray(new String[0]);
	}

	@Override
	public boolean canReturnKeys() {
		return keyed;
	}

	@Override
	public boolean areKeysRecommended() {
		return false;
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
	public Class<?>[] possibleReturnTypes() {
		return unfilteredObjects.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		return unfilteredObjects.canReturn(returnType);
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
		return keyed;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

}
