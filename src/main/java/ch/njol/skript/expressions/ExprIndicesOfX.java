package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("Indices of X in List")
@Description("Returns all the indices of a list where their value is X.")
@Examples({
	"set {_list::*} to 1, 2, 3, 1, 2, 3",
	"set {_indices::*} to the indices of the value 1 in {_list::*}",
	"# {_indices::*} is now 1, 4",
	"",
	"set {_indices::*} to the indices of the value 2 in {_list::*}",
	"# {_indices::*} is now 2, 5",
	"",
	"set {_otherlist::burb} to 100",
	"set {_otherlist::burp} to 100",
	"set {_indices::*} to the first index of the value 100 in {_otherlist::*}",
	"# {_indices::*} is now burb",
	"set {_indices::*} to the last index of the value 100 in {_otherlist::*}",
	"# {_indices::*} is now burp"
})
@Since("INSERT VERSION")
public class ExprIndicesOfX extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprIndicesOfX.class, String.class, ExpressionType.COMBINED,
			"[the] [1:first|2:last] (indices|index[es]) of [[the] value] %object% in %objects%"
		);
	}

	private IndexType type;
	private Expression<?> value;
	private Variable<?> list;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!(exprs[1] instanceof Variable<?> var) || !var.isList()) {
			Skript.error("'" + exprs[1].toString(null, false) +
				"' can only ever have one value at most, thus the 'indices of x in ...' expression is useless.");
			return false;
		}

		list = var;
		type = IndexType.get(parseResult.mark);
		value = LiteralUtils.defendExpression(exprs[0]);

		return LiteralUtils.canInitSafely(value);
	}

	@Override
	protected String @Nullable [] get(Event event) {
		Object value = this.value.getSingle(event);
		if (value == null)
			return null;

		//noinspection unchecked
		Map<String, Object> variable = (Map<String, Object>) list.getRaw(event);
		if (variable == null)
			return null;

		List<String> indices = new ArrayList<>();

		for (Map.Entry<String, Object> entry : variable.entrySet()) {
			Object entryValue = entry.getValue();
			if (entryValue instanceof Map<?, ?> map)
				entryValue = map.get(null);

			if (entryValue.equals(value))
				indices.add(entry.getKey());
		}

		if (indices.isEmpty())
			return null;

		if (type == IndexType.FIRST)
			return new String[]{indices.get(0)};
		else if (type == IndexType.LAST)
			return new String[]{indices.get(indices.size() - 1)};
		return indices.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return type == IndexType.FIRST || type == IndexType.LAST;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append(type.toString());
		if (type != IndexType.ALL)
			builder.append("indices");
		else
			builder.append("index");
		builder.append("of value", value, "in", list);

		return builder.toString();
	}

	private enum IndexType {
		FIRST, LAST, ALL;

		public static IndexType get(int mark) {
			return switch (mark) {
				case 1 -> FIRST;
				case 2 -> LAST;
				default -> ALL;
			};
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

}
