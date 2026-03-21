package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
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
	"Returns the current accumulated/reduced value within a <a href='#ExprReduce'>reduce expression</a>.",
	"This represents the result of all previous reduction operations.",
	"Can only be used inside the reduce expression's operation block."
})
@Example("set {_sum} to {_numbers::*} reduced with [reduced value + input]")
@Example("set {_max} to {_values::*} reduced with [reduced value if reduced value > input else input]")
@Example("set {_combined} to {_items::*} reduced with (\"%reduced value%, %input%\")")
@Since("INSERT VERSION")
public class ExprReducedValue extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprReducedValue.class, Object.class, ExpressionType.SIMPLE,
			"[the] reduced value",
			"[the] (accumulator|accumulated) [value]",
			"[the] folded value"
		);
	}

	private ExprReduce reduce;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		InputSource inputSource = getParser().getData(InputData.class).getSource();
		if (!(inputSource instanceof ExprReduce exprReduce)) {
			Skript.error("The 'reduced value' expression can only be used within a reduce operation");
			return false;
		}

		this.reduce = exprReduce;
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		Object reducedValue = reduce.getReducedValue();
		return reducedValue == null ? new Object[0] : new Object[] { reducedValue };
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "reduced value";
	}

}
