package ch.njol.skript.expressions;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

@Name("Indices of Value")
@Description({
	"Get the first, last or all positions of a character (or text) in another text using "
		+ "'positions of %texts% in %text%'. Nothing is returned when the value does not occur in the text. "
		+ "Positions range from 1 to the <a href='#ExprIndicesOf'>length</a> of the text (inclusive).",
	"",
	"Using 'indices/positions of %objects% in %objects%', you can get the indices or positions of "
		+ "a list where the value at that index is the provided value. "
		+ "Indices are only supported for keyed expressions (e.g. variable lists) "
		+ "and will return the string indices of the given value. "
		+ "Positions can be used with any list and will return "
		+ "the numerical position of the value in the list, counting up from 1. "
		+ "Additionally, nothing is returned if the value is not found in the list.",
	"",
	"Whether string comparison is case-sensitive or not can be configured in Skript's config file.",
})
@Example("""
	set {_first} to the first position of "@" in the text argument
	if {_s} contains "abc":
		set {_s} to the first (position of "abc" in {_s} + 3) characters of {_s}
		# removes everything after the first "abc" from {_s}
	""")
@Example("""
	set {_list::*} to 1, 2, 3, 1, 2, 3
	set {_indices::*} to indices of the value 1 in {_list::*}
	# {_indices::*} is now "1" and "4"

	set {_indices::*} to all indices of the value 2 in {_list::*}
	# {_indices::*} is now "2" and "5"

	set {_positions::*} to all positions of the value 3 in {_list::*}
	# {_positions::*} is now 3 and 6
	""")
@Example("""
	set {_otherlist::bar} to 100
	set {_otherlist::hello} to "hi"
	set {_otherlist::burb} to 100
	set {_otherlist::tud} to "hi"
	set {_otherlist::foo} to 100

	set {_indices::*} to the first index of the value 100 in {_otherlist::*}
	# {_indices::*} is now "bar"

	set {_indices::*} to the last index of the value 100 in {_otherlist::*}
	# {_indices::*} is now "foo"

	set {_positions::*} to all positions of the value 100 in {_otherlist::*}
	# {_positions::*} is now 1, 3 and 5

	set {_positions::*} to all positions of the value "hi" in {_otherlist::*}
	# {_positions::*} is now 2 and 4
	""")
@Since("2.1, 2.12 (indices, positions of list)")
public class ExprIndicesOfValue extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprIndicesOfValue.class, Object.class, ExpressionType.COMBINED,
			"[the] [1:first|2:last|3:all] (position[mult:s]|mult:indices|index[mult:es]) of [[the] value] %strings% in %string%",
			"[the] [1:first|2:last|3:all] position[mult:s] of [[the] value] %objects% in %~objects%",
			"[the] [1:first|2:last|3:all] (mult:indices|index[mult:es]) of [[the] value] %objects% in %~objects%"
		);
	}

	private IndexType indexType;
	private boolean position, string;
	private Expression<?> needle, haystack;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[1].isSingle() && (matchedPattern > 0)) {
			Skript.error("'" + exprs[1] + "' can only ever have one value at most, "
				+ "thus the 'indices of x in list' expression has no effect.");
			return false;
		}

		if (!KeyProviderExpression.canReturnKeys(exprs[1]) && matchedPattern == 2) {
			Skript.error("'" + exprs[1] + "' is not a keyed expression. "
				+ "You can only get the indices of a keyed expression.");
			return false;
		}

		indexType = IndexType.values()[parseResult.mark == 0 ? 0 : parseResult.mark - 1];
		if (parseResult.mark == 0 && parseResult.hasTag("mult"))
			indexType = IndexType.ALL;

		position = matchedPattern <= 1;
		string = matchedPattern == 0;
		needle = LiteralUtils.defendExpression(exprs[0]);
		haystack = exprs[1];

		return LiteralUtils.canInitSafely(needle);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object[] needle = this.needle.getAll(event);
		if (needle.length == 0)
			return position ? new Long[0] : new String[0];

		if (!this.position) {
			assert haystack instanceof KeyProviderExpression<?>;
			return getIndices((KeyProviderExpression<?>) haystack, needle, event);
		}

		if (!string)
			return getListPositions(haystack, needle, event);

		String haystack = (String) this.haystack.getSingle(event);
		if (haystack == null)
			return new Long[0];

		return getStringPositions(haystack, (String[]) needle);
	}

	/**
	 * Get the positions of needles in a haystack string
	 * @param haystack the haystack
	 * @param needles the needles
	 * @return the found positions
	 */
	private Long[] getStringPositions(String haystack, String[] needles) {
		boolean caseSensitive = SkriptConfig.caseSensitive.value();

		List<Long> positions = new ArrayList<>();
		for (String needle : needles) {
			long position = StringUtils.indexOf(haystack, needle, caseSensitive);
			if (position == -1)
				continue;

			switch (indexType) {
				case FIRST -> positions.add(position + 1);
				case LAST -> positions.add((long) StringUtils.lastIndexOf(haystack, needle, caseSensitive));
				case ALL -> {
					do {
						positions.add(position + 1);
						position = StringUtils.indexOf(haystack, needle, (int) position + 1, caseSensitive);
					} while (position != -1);
				}
			}
		}
		return positions.toArray(Long[]::new);
	}

	/**
	 * Generic method to get the positions/indices of needles in a haystack
	 * @param haystackIterator the haystack
	 * @param needles the values to look for in the haystack
	 * @param valueMapper maps an item in the haystack to its value
	 * @param indexMapper maps an item in the haystack to its index/position
	 * @param arrayFactory factory to create the resulting array
	 * @return the found indices/positions
	 * @param <Item> the type of items in the haystack
	 * @param <Index> the type of the resulting indices/positions
	 * @param <Value> the type of values to look for
	 */
	private <Item, Index, Value> Index[] getMatches(
		Iterator<Item> haystackIterator,
		Value[] needles,
		Function<Item, Value> valueMapper,
		BiFunction<Item, Long, Index> indexMapper,
		IntFunction<Index[]> arrayFactory
	) {
		boolean caseSensitive = SkriptConfig.caseSensitive.value();

		//noinspection unchecked
		List<Index>[] results = new List[needles.length];
		long index = 1;
		boolean shouldBreak = false;
		while (haystackIterator.hasNext()) {
			Item item = haystackIterator.next();
			for (int i = 0; i < needles.length; i++) {
				Object needle = needles[i];
				if (!equals(valueMapper.apply(item), needle, caseSensitive))
					continue;

				Index mappedIndex = indexMapper.apply(item, index);
				switch (indexType) {
					case FIRST, LAST -> results[i] = Collections.singletonList(mappedIndex);
					case ALL -> {
						if (results[i] == null)
							results[i] = new ArrayList<>();
						results[i].add(mappedIndex);
					}
				}
			}
			// break early if all first indices were found
			if (indexType == IndexType.FIRST && !ArrayUtils.contains(results, null))
				break;
			index++;
		}

		return Arrays.stream(results)
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.toArray(arrayFactory);
	}

	/**
	 * Get the positions of needles in a haystack list
	 * @param haystack the haystack
	 * @param needles the needles
	 * @param event the event
	 * @return the found positions
	 */
	private Long[] getListPositions(Expression<?> haystack, Object[] needles, Event event) {
		Iterator<?> haystackIterator = haystack.iterator(event);
		if (haystackIterator == null)
			return new Long[0];

		return getMatches(haystackIterator, needles, item -> item, (item, index) -> index, Long[]::new);
	}

	/**
	 * Get the indices of needles in a haystack keyed list
	 * @param haystack the haystack
	 * @param needles the needles
	 * @param event the event
	 * @return the found indices
	 */
	private String[] getIndices(KeyProviderExpression<?> haystack, Object[] needles, Event event) {
		Iterator<? extends KeyedValue<?>> haystackIterator = haystack.keyedIterator(event);
		if (haystackIterator == null)
			return new String[0];

		return getMatches(haystackIterator, needles, KeyedValue::value, (item, index) -> item.key(), String[]::new);
	}

	private boolean equals(Object key, Object value, boolean caseSensitive) {
		if (key instanceof String keyString && value instanceof String valueString)
			return StringUtils.equals(keyString, valueString, caseSensitive);
		return key.equals(value);
	}

	@Override
	public boolean isSingle() {
		return (indexType == IndexType.FIRST || indexType == IndexType.LAST) && needle.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		if (position)
			return Long.class;
		return String.class;
	}

	@Override
	public Expression<?> simplify() {
		if (this.position && this.string
			&& needle instanceof Literal<?> && haystack instanceof Literal<?>
		) {
			return SimplifiedLiteral.fromExpression(this);
		}
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(indexType.name().toLowerCase(Locale.ENGLISH));
		if (position) {
			builder.append("positions");
		} else {
			builder.append("indices");
		}
		builder.append("of value", needle, "in", haystack);

		return builder.toString();
	}

	private enum IndexType {
		FIRST, LAST, ALL
	}

}
