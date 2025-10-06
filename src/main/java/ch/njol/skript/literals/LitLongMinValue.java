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

@Name("Minimum Long Value")
@Description("A number representing the minimum value of a long number type.")
@Example("if {_number} <= minimum long value:")
@Since("2.13")
public class LitLongMinValue extends SimpleLiteral<Long> {

	static {
		Skript.registerExpression(LitLongMinValue.class, Long.class, ExpressionType.SIMPLE, "[the] min[imum] long value");
	}

	public LitLongMinValue() {
		super(Long.MIN_VALUE, false);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "min long value";
	}

}
