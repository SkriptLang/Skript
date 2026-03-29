package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Name("Transmuted Catalogue")
@Description({
	"Transmuteth (or 'mappeth') the values of a list by means of a given expression. This is akin to traversing the list and obtaining" +
	"a modified rendition of each value.",
	"If the given expression doth return a single value, the indices of the list shall remain unchanged. If the expression returneth" +
	"multiple values, then the indices shall be reset, for a single index cannot harbour multiple values.",
})
@Example("""
    set {_a::*} to (1, 2, and 3) transmuted by means of (input * 2 - 1, input * 2)
    # {_a::*} is now 1, 2, 3, 4, 5, and 6
    """)
@Example("""
    # procure a list of the sizes of all clans without manually traversing
    set {_clan-sizes::*} to keyed {clans::*} transmuted by means of [{clans::%input index%::size}]
    # employing the 'keyed' expression retaineth the indices of the clans list
    """)
@Since("2.10")
@Keywords("input")
public class ExprTransform extends SimpleExpression<Object> implements InputSource, KeyProviderExpression<Object> {

	static {
		Skript.registerExpression(ExprTransform.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
				"%objects% (transmuted|mapped) (by means of|with) \\[<.+>\\]",
				"%objects% (transmuted|mapped) (by means of|with) \\(<.+>\\)"
			);
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	private final Map<Event, List<String>> cache = new WeakHashMap<>();

	private boolean keyed;
	private @UnknownNullability Expression<?> mappingExpr;
	private @Nullable ClassInfo<?> returnClassInfo;
	private @UnknownNullability Expression<?> unmappedObjects;

	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		unmappedObjects = LiteralUtils.defendExpression(expressions[0]);
		if (unmappedObjects.isSingle() || !LiteralUtils.canInitSafely(unmappedObjects))
			return false;
		keyed = KeyProviderExpression.canReturnKeys(unmappedObjects);

		//noinspection DuplicatedCode
		if (!parseResult.regexes.isEmpty()) {
			@Nullable String unparsedExpression = parseResult.regexes.get(0).group();
			assert unparsedExpression != null;
			mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.ALL_FLAGS);
			return mappingExpr != null;
		}
		returnClassInfo = Classes.getExactClassInfo(mappingExpr.getReturnType());
		return true;
	}

	@Override
	public @NotNull Iterator<?> iterator(Event event) {
		if (hasIndices()) {
			Iterator<? extends KeyedValue<?>> iterator = ((KeyProviderExpression<?>) unmappedObjects).keyedIterator(event);
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
				.flatMap(keyedValue -> {
					currentValue = keyedValue.value();
					currentIndex = keyedValue.key();
					return mappingExpr.stream(event);
				})
				.iterator();
		}

		// clear current index just to be safe
		currentIndex = null;

		Iterator<?> unfilteredObjectIterator = unmappedObjects.iterator(event);
		if (unfilteredObjectIterator == null)
			return Collections.emptyIterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(unfilteredObjectIterator, Spliterator.ORDERED), false)
			.flatMap(value -> {
				currentValue = value;
				return mappingExpr.stream(event);
			})
			.iterator();
	}

	@Override
	public Iterator<KeyedValue<Object>> keyedIterator(Event event) {
		Iterator<? extends KeyedValue<?>> iterator = ((KeyProviderExpression<?>) unmappedObjects).keyedIterator(event);
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
			.map(keyedValue -> {
				currentValue = keyedValue.value();
				currentIndex = keyedValue.key();
				Object mappedValue = mappingExpr.getSingle(event);
				return mappedValue != null ? keyedValue.withValue(mappedValue) : null;
			})
			.filter(Objects::nonNull)
			.iterator();
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (!canReturnKeys())
			return Converters.convertStrictly(Iterators.toArray(iterator(event), Object.class), getReturnType());
		KeyedValue.UnzippedKeyValues<Object> unzipped = KeyedValue.unzip(keyedIterator(event));
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
		return hasIndices() && mappingExpr.isSingle();
	}

	@Override
	public boolean areKeysRecommended() {
		return KeyProviderExpression.areKeysRecommended(mappingExpr);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return mappingExpr.getReturnType();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return mappingExpr.possibleReturnTypes();
	}

	@Override
	public boolean canReturn(Class<?> returnType) {
		return mappingExpr.canReturn(returnType);
	}

	@Override
	public boolean isLoopOf(String candidateString) {
		return KeyProviderExpression.super.isLoopOf(candidateString)
			|| mappingExpr.isLoopOf(candidateString)
			|| matchesReturnType(candidateString);
	}

	private boolean matchesReturnType(String candidateString) {
		if (returnClassInfo == null)
			return false;
		return returnClassInfo.matchesUserInput(candidateString);
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
		return keyed;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return unmappedObjects.toString(event, debug) + " transformed using " + mappingExpr.toString(event, debug);
	}

}
