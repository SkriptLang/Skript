package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.InputSource.InputData;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Reduced Value")
@Description({
	"Returns the current accumulated/reduced value within a reduce expression.",
	"This represents the result of all previous reduction operations.",
	"Can only be used inside the reduce expression's operation block."
})
@Examples({
	"set {_sum} to {_numbers::*} reduced with [reduced value + input]",
	"set {_max} to {_values::*} reduced with [reduced value if reduced value > input else input]",
	"set {_combined} to {_items::*} reduced with [\"%reduced value%, %input%\"]"
})
@Since("INSERT VERSION")
public class ExprReducedValue extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprReducedValue.class, Object.class, ExpressionType.SIMPLE,
			"reduced value",
			"(accumulator|accumulated) [value]",
			"fold[ed] value"
		);
	}

	private InputSource inputSource;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		inputSource = getParser().getData(InputData.class).getSource();
		if (inputSource == null) {
			Skript.error("The 'reduced value' expression can only be used within a reduce operation");
			return false;
		}
		if (!(inputSource instanceof ExprReduce)) {
			Skript.error("The 'reduced value' expression can only be used within a reduce operation");
			return false;
		}
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (!(inputSource instanceof ExprReduce reduce))
			return null;

		Object reducedValue = reduce.getReducedValue();
		return reducedValue != null ? new Object[] { reducedValue } : null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		if (inputSource instanceof ExprReduce reduce) {
			Class<?> returnType = reduce.getReturnType();
			return returnType != null ? returnType : Object.class;
		}
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "reduced value";
	}

}