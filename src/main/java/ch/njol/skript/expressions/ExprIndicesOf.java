package ch.njol.skript.expressions;

import ch.njol.skript.lang.*;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Pair;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import java.lang.reflect.Array;
import java.util.*;

@Name("Indices Of")
@Description({
	"Get the first, last or all positions of a character (or text) in another text using "
		+ "'positions of %text% in %text%'. Nothing is returned when the value does not occur in the text. "
		+ "Positions range from 1 to the <a href='#ExprIndicesOf'>length</a> of the text (inclusive).",
	"",
	"Using 'indices/positions of %objects% in %objects%', you can get the indices or positions of "
		+ "a list where the value at that index is the provided value. "
		+ "Indices are only supported for variable lists and will return the string indices of the given value. "
		+ "Positions can be used with any list and will return "
		+ "the numerical position of the value in the list, counting up from 1. "
		+ "As well, nothing is returned if the value is not found in the list."
})
@Examples({
	"set {_first} to the first position of \"@\" in the text argument",
	"if {_s} contains \"abc\":",
		"\tset {_s} to the first (position of \"abc\" in {_s} + 3) characters of {_s}",
		"\t# removes everything after the first \"abc\" from {_s}",
	"",
	"set {_list::*} to 1, 2, 3, 1, 2, 3",
	"set {_indices::*} to indices of the value 1 in {_list::*}",
	"# {_indices::*} is now \"1\" and \"4\"",
	"",
	"set {_indices::*} to all indices of the value 2 in {_list::*}",
	"# {_indices::*} is now \"2\" and \"5\"",
	"",
	"set {_positions::*} to all positions of the value 3 in {_list::*}",
	"# {_positions::*} is now 3 and 6",
	"",
	"set {_otherlist::bar} to 100",
	"set {_otherlist::hello} to \"hi\"",
	"set {_otherlist::burb} to 100",
	"set {_otherlist::tud} to \"hi\"",
	"set {_otherlist::foo} to 100",
	"",
	"set {_indices::*} to the first index of the value 100 in {_otherlist::*}",
	"# {_indices::*} is now \"bar\"",
	"set {_indices::*} to the last index of the value 100 in {_otherlist::*}",
	"# {_indices::*} is now \"foo\"",
	"",
	"set {_positions::*} to all positions of the value 100 in {_otherlist::*}",
	"# {_positions::*} is now 1, 3 and 5",
	"set {_positions::*} to all positions of the value \"hi\" in {_otherlist::*}",
	"# {_positions::*} is now 2 and 4"
})
@Since("2.1, INSERT VERSION (indices, positions of list)")
public class ExprIndicesOf extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprIndicesOf.class, Object.class, ExpressionType.COMBINED,
			"[the] [1:first|2:last|3:all] (position[mult:s]|mult:indices|index[mult:es]) of [[the] value] %string% in %string%",
			"[the] [1:first|2:last|3:all] position[mult:s] of [[the] value] %object% in %~objects%",
			"[the] [1:first|2:last|3:all] (mult:indices|index[mult:es]) of [[the] value] %object% in %~objects%"
		);
	}

	private IndexType indexType;
	private boolean position, string;
	private Expression<?> value, objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[1].isSingle() && (matchedPattern > 0)) {
			Skript.error("'" + exprs[1] + "' can only ever have one value at most, "
				+ "thus the 'indices of x in list' expression has no effect.");
			return false;
		}

		if (!(exprs[1] instanceof Variable<?>) && matchedPattern == 2) {
			Skript.error("'" + exprs[1] + "' is not a list variable. "
				+ "You can only get the indices of a list variable.");
			return false;
		}

		indexType = IndexType.values()[parseResult.mark == 0 ? 0 : parseResult.mark - 1];
		if (parseResult.mark == 0 && parseResult.hasTag("mult"))
			indexType = IndexType.ALL;

		position = matchedPattern <= 1;
		string = matchedPattern == 0;
		value = LiteralUtils.defendExpression(exprs[0]);
		objects = exprs[1];

		return LiteralUtils.canInitSafely(value);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object value = this.value.getSingle(event);
		if (value == null)
			return (Object[]) Array.newInstance(getReturnType(), 0);

		if (this.position) {
			if (string) {
				String haystack = (String) objects.getSingle(event);
				if (haystack == null)
					return new Long[0];

				return getStringPositions(haystack, (String) value);
			}

			return getListPositions(objects, value, event);
		}

		assert objects instanceof Variable<?>;

		return getVariableIndices((Variable<?>) objects, value, event);
	}

	private Long[] getStringPositions(String haystack, String needle) {
		List<Long> positions = new ArrayList<>();
		long position = haystack.indexOf(needle);

		if (position == -1)
			return new Long[0];

		if (indexType == IndexType.ALL) {
			while (position != -1) {
				positions.add(position + 1);
				position = haystack.indexOf(needle, (int) position + 1);
			}
			return positions.toArray(new Long[0]);
		}

		if (indexType == IndexType.LAST)
			position = haystack.lastIndexOf(needle);

		return new Long[]{position + 1};
	}

	private Long[] getListPositions(Expression<?> list, Object value, Event event) {
		Iterator<?> iterator = list.iterator(event);
		if (iterator == null)
			return new Long[0];

		List<Long> positions = new ArrayList<>();

		long position = 0;
		while (iterator.hasNext()) {
			position++;

			if (!iterator.next().equals(value))
				continue;

			if (indexType == IndexType.FIRST)
				return new Long[]{position};

			positions.add(position);
		}

		if (indexType == IndexType.LAST)
			return new Long[]{positions.get(positions.size() - 1)};

		return positions.toArray(Long[]::new);
	}

	private String[] getVariableIndices(Variable<?> variable, Object value, Event event) {
		Iterator<Pair<String, Object>> iterator = variable.variablesIterator(event);
		if (iterator == null)
			return new String[0];

		List<String> indices = new ArrayList<>();

		while (iterator.hasNext()) {
			var pair = iterator.next();

			Object pairValue = pair.getValue();
			// when {foo::1::bar} is set, the value of {foo::1} is a map with a null key that holds the value of {foo::1}
			if (pairValue instanceof Map<?, ?> map)
				pairValue = map.get(null);

			if (pairValue.equals(value)) {
				if (indexType == IndexType.FIRST)
					return new String[]{pair.getKey()};

				indices.add(pair.getKey());
			}
		}

		if (indices.isEmpty())
			return new String[0];

		if (indexType == IndexType.LAST)
			return new String[]{indices.get(indices.size() - 1)};

		return indices.toArray(String[]::new);
	}

	@Override
	public boolean isSingle() {
		return indexType == IndexType.FIRST || indexType == IndexType.LAST;
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
			&& value instanceof Literal<?> && objects instanceof Literal<?>
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
		builder.append("of value", value, "in", objects);

		return builder.toString();
	}

	private enum IndexType {
		FIRST, LAST, ALL
	}

}
