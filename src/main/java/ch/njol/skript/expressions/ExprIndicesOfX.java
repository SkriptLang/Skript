package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("Indices of X in List")
@Description(
		"Returns the indices or positions of a list where the value at that index is the provided value. " +
		"Getting the indices of a list will return strings, whereas the positions will return a number."
)
@Examples({
		"set {_list::*} to 1, 2, 3, 1, 2, 3",
		"set {_indices::*} to the indices of the value 1 in {_list::*}",
		"# {_indices::*} is now \"1\", \"4\"",
		"",
		"set {_indices::*} to the indices of the value 2 in {_list::*}",
		"# {_indices::*} is now \"2\", \"5\"",
		"",
		"set {_positions::*} to the positions of the value 3 in {_list::*}",
		"# {_positions::*} is now 3, 6",
		"",
		"set {_otherlist::burb} to 100",
		"set {_otherlist::burp} to 100",
		"set {_otherlist::brup} to 100",
		"set {_indices::*} to the first index of the value 100 in {_otherlist::*}",
		"# {_indices::*} is now \"burb\"",
		"set {_indices::*} to the last index of the value 100 in {_otherlist::*}",
		"# {_indices::*} is now \"burp\"",
		"",
		"set {_positions::*} to the positions of the value 100 in {_otherlist::*}",
		"# {_positions::*} is now 1, 2, 3",
})
@Since("INSERT VERSION")
public class ExprIndicesOfX extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprIndicesOfX.class, Object.class, ExpressionType.COMBINED,
			"[the] [1:first|2:last] (indices|index[es]|:position[s]) of [[the] value] %object% in %objects%"
		);
	}

	private IndexType type;
	private boolean position;
	private Expression<?> value;
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[1].isSingle() || (exprs[1] instanceof Variable<?> var && !var.isList())) {
			Skript.error("'" + exprs[1].toString(null, false) +
					"' can only ever have one value at most, thus the 'indices of x in list' expression has no effect.");
			return false;
		}

		position = parseResult.hasTag("position");
		objects = LiteralUtils.defendExpression(exprs[1]);
		type = IndexType.values()[parseResult.mark];
		value = LiteralUtils.defendExpression(exprs[0]);

		return LiteralUtils.canInitSafely(objects, value);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object value = this.value.getSingle(event);
		if (value == null)
			return new String[0];

		List<Object> indices = new ArrayList<>();

		int count = 1;
		if (objects instanceof Variable<?> list) {
			//noinspection unchecked
			Map<String, Object> variable = (Map<String, Object>) list.getRaw(event);
			if (variable == null)
				return new String[0];

			for (Map.Entry<String, Object> entry : variable.entrySet()) {
				Object entryValue = entry.getValue();
				if (entryValue instanceof Map<?, ?> map)
					entryValue = map.get(null);

				if (entryValue.equals(value)) {
					if (position)
						indices.add(count);
					else
						indices.add(entry.getKey());
				}
				count++;
			}
		} else {
			for (Object object : objects.getArray(event)) {
				if (object.equals(value)) {
					if (position)
						indices.add(count);
					else
						indices.add(String.valueOf(count));
				}
				count++;
			}
		}

		if (indices.isEmpty())
			return new String[0];

		if (type == IndexType.FIRST)
			return new Object[]{indices.get(0)};
		else if (type == IndexType.LAST)
			return new Object[]{indices.get(indices.size() - 1)};
		return indices.toArray();
	}

	@Override
	public boolean isSingle() {
		return type == IndexType.FIRST || type == IndexType.LAST;
	}

	@Override
	public Class<?> getReturnType() {
		if (position)
			return Integer.class;
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(type.name().toLowerCase());
		if (type == IndexType.ALL)
			builder.append("indices");
		else if (position)
			builder.append("positions");
		else
			builder.append("index");
		builder.append("of value", value, "in", objects);

		return builder.toString();
	}

	private enum IndexType {
		ALL, FIRST, LAST
	}

}
