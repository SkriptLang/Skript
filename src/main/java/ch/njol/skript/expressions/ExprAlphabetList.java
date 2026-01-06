package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.common.collect.Iterators;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

@Name("Alphabetical Sort")
@Description("Sorts given strings in alphabetical order.")
@Example("set {_list::*} to alphabetically sorted {_strings::*}")
@Since("2.2-dev18b, INSERT VERSION (retain indices when looping)")
public class ExprAlphabetList extends SimpleExpression<String> implements KeyedIterableExpression<String> {

	static{
		Skript.registerExpression(ExprAlphabetList.class, String.class, ExpressionType.COMBINED, "alphabetically sorted %strings%");
	}

	private Expression<String> texts;
	private boolean keyed;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		texts = (Expression<String>) expressions[0];
		if (texts.isSingle()) {
			Skript.error("A single string cannot be sorted.");
			return false;
		}
		keyed = KeyedIterableExpression.canIterateWithKeys(texts);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		String[] sorted = texts.getArray(event);
		Arrays.sort(sorted);
		return sorted;
	}

	@Override
	public boolean canIterateWithKeys() {
		return keyed;
	}

	@Override
	public Iterator<KeyedValue<String>> keyedIterator(Event event) {
		if (!keyed)
			throw new UnsupportedOperationException();
		var iterator = ((KeyedIterableExpression<String>) texts).keyedIterator(event);
		//noinspection unchecked
		KeyedValue<String>[] array = Iterators.toArray(iterator, KeyedValue.class);
		Arrays.sort(array, Comparator.comparing(KeyedValue::value));
		return Iterators.forArray(array);
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public boolean isIndexLoop(String input) {
		if (!keyed)
			throw new IllegalStateException();
		return ((KeyedIterableExpression<String>) texts).isIndexLoop(input);
	}

	@Override
	public boolean isLoopOf(String input) {
		return texts.isLoopOf(input);
	}

	@Override
	public Expression<? extends String> simplify() {
		if (texts instanceof Literal<String>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "alphabetically sorted " + texts.toString(event, debug);
	}

}
