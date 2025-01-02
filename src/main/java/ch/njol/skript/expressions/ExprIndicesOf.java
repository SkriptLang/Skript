package ch.njol.skript.expressions;

import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.LiteralUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Peter Güttinger
 */
@Name("Indices Of")
@Description({
	"Get the first, last or all positions of a character (or text) in another text using "
		+ "'positions of %text% in %text%'. -1 is returned when the value does not occur in the text."
		+ "Positions range from 1 to the <a href='#ExprIndicesOf'>length</a> of the text.",
	"",
	"Using 'indices/positions of %objects%', you can get the indices or positions of a list where the value at that index is the provided value. "
		+ "Indices are only supported for variable lists and will return the string indices of the given value. "
		+ "Positions can be used with any list and will return the numerical position of the value in the list, counting up from 1."
		+ "Note that nothing is returned if the value is not found in the list."
})
@Examples({
	"set {_first} to the first index of \"@\" in the text argument",
	"if {_s} contains \"abc\":",
		"\tset {_s} to the first (index of \"abc\" in {_s} + 3) characters of {_s} # removes everything after the first \"abc\" from {_s}",
	"",
	"set {_list::*} to 1, 2, 3, 1, 2, 3",
	"set {_indices::*} to all indices of the value 1 in {_list::*}",
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
			"[the] [first|1:last|2:all] (position[s]|indices|index[es]) of [[the] value] %string% in %string%",
			"[the] [first|1:last|2:all] (indices|index[es]) of [[the] value] %object% in %~objects%",
			"[the] [first|1:last|2:all] position[s] of [[the] value] %object% in %~objects%"
		);
	}
	
	private IndexType type;
	private boolean position;
	private boolean string;
	private Expression<?> value, objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[1].isSingle() && (matchedPattern == 1 || matchedPattern == 2)) {
			Skript.error("'" + exprs[1] + "' can only ever have one value at most, thus the 'indices of x in list' expression has no effect.");
			return false;
		}

		if (!(exprs[1] instanceof Variable<?>) && matchedPattern == 1) {
			Skript.error("'" + exprs[1] + "' is not a list variable. You can only get the indices of a list variable.");
			return false;
		}

		type = IndexType.values()[parseResult.mark];
		position = matchedPattern == 0 || matchedPattern == 2;
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
			List<Long> positions = new ArrayList<>();

			if (string) {
				String needle = (String) value;
				String haystack = (String) objects.getSingle(event);
				if (haystack == null)
					return new Long[0];

				long position = haystack.indexOf(needle);

				if (type == IndexType.ALL) {
					while (position != -1) {
						positions.add(position + 1);
						position = haystack.indexOf(needle, (int) position + 1);
					}
					return positions.toArray();
				}

				if (type == IndexType.LAST) {
					position = haystack.lastIndexOf(needle);
				}

				return new Long[]{(position == -1 ? -1 : position + 1)};
			}

			long position = 1;
			for (Object object : objects.getArray(event)) {
				if (object.equals(value)) {
					if (type == IndexType.FIRST)
						return new Long[]{position};
					positions.add(position);
				}
				position++;
			}

			if (type == IndexType.LAST)
				return new Long[]{positions.get(positions.size() - 1)};
			return positions.toArray();
		}

		assert objects instanceof Variable<?>;

		Variable<?> list = (Variable<?>) objects;
		//noinspection unchecked
		Map<String, Object> variable = (Map<String, Object>) list.getRaw(event);
		if (variable == null)
			return new String[0];

		List<String> indices = new ArrayList<>();

		for (Entry<String, Object> entry : variable.entrySet()) {
			Object entryValue = entry.getValue();
			// the value of {foo::1} when {foo::1::bar} is set is a map with a null key of the value {foo::1}
			if (entryValue instanceof Map<?, ?> map)
				entryValue = map.get(null);

			if (entryValue.equals(value)) {
				if (type == IndexType.FIRST)
					return new String[]{entry.getKey()};
				indices.add(entry.getKey());
			}
		}

		if (indices.isEmpty())
			return new String[0];

		if (type == IndexType.LAST)
			return new String[]{indices.get(indices.size() - 1)};
		return indices.toArray();
	}
	
	@Override
	public boolean isSingle() {
		return type == IndexType.FIRST || type == IndexType.LAST;
	}
	
	@Override
	public Class<?> getReturnType() {
		if (position)
			return Long.class;
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(type.name().toLowerCase());
		if (position) {
			builder.append("positions");
		} else {
			builder.append("index");
		}
		builder.append("of value", value, "in", objects);

		return builder.toString();
	}

	private enum IndexType {
		FIRST, LAST, ALL
	}
	
}
