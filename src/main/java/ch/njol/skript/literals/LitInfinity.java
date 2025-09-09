package ch.njol.skript.literals;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Infinity")
@Description("A number representing positive infinity.")
@Example("if {_number} is infinity:")
@Since("2.2-dev32d")
public class LitInfinity extends SimpleLiteral<Double> {

	static {
		// patterns are a bit messy to avoid conflicts between `infinity` enchantment and `infinity` number.
		Skript.registerExpression(LitInfinity.class, Double.class, ExpressionType.SIMPLE,
				"positive (infinity|∞) [value]",
				"∞ [value]",
				"infinity value",
				"value of [positive] (infinity|∞)");
	}

	public LitInfinity() {
		super(Double.POSITIVE_INFINITY, false);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "infinity";
	}

}
