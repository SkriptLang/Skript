package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Name("Transformed List")
@Description({
	"Transforms (or 'maps') a list's values using a given expression. This is akin to looping over the list and getting " +
	"a modified version of each value.",
	"Indices cannot be retained with this expression. To retain indices, see the transform effect."
})
@Examples({
	"set {_a::*} to (1, 2, and 3) transformed using (input * 2 - 1, input * 2)",
	"# {_a::*} is now 1, 2, 3, 4, 5, and 6",
	"",
	"# get a list of the sizes of all clans without manually looping",
	"set {_clan-sizes::*} to indices of {clans::*} transformed using [{clans::%input%::size}]",
})
@Since("INSERT VERSION")
@Keywords("input")
public class ExprTransform extends SimpleExpression<Object> implements InputSource {

	static {
		Skript.registerExpression(ExprTransform.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
				"%objects% (transformed|mapped) (using|with) \\[<.+>\\]",
				"%objects% (transformed|mapped) (using|with) \\(<.+>\\)"
			);
		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

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
		if (unmappedObjects instanceof Variable<?> variable) {
			Iterator<Pair<String, Object>> variableIterator = variable.variablesIterator(event);
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(variableIterator, Spliterator.ORDERED), false)
				.flatMap(pair -> {
					currentValue = pair.getValue();
					currentIndex = pair.getKey();
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
	protected Object @Nullable [] get(Event event) {
		try {
			return Converters.convertStrictly(Iterators.toArray(iterator(event), Object.class), getReturnType());
		} catch (ClassCastException e1) {
			return (Object[]) Array.newInstance(getReturnType(), 0);
		}
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
	public boolean isLoopOf(String candidateString) {
		return mappingExpr.isLoopOf(candidateString) || matchesReturnType(candidateString);
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
		return unmappedObjects instanceof Variable<?>;
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
