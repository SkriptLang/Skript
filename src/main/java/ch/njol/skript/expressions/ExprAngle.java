package ch.njol.skript.expressions;

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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Angle")
@Description({
	"Represents the passed number value in degrees.",
	"If radians is specified, converts the passed value to degrees. This conversion may not be entirely accurate, " +
		"due to floating point precision.",
})
@Examples({
	"set {_angle} to 90 degrees",
	"{_angle} is 90 # true",
	"180 degrees is pi # true",
	"pi radians is 180 degrees # true"
})
@Since("INSERT VERSION")
public class ExprAngle extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprAngle.class, Number.class, ExpressionType.SIMPLE,
			"%number% deg[ree][s]",
			"%number% rad[ian][s]");
	}

	private Expression<Number> angle;
	private boolean isRadians;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		angle = (Expression<Number>) expressions[0];
		isRadians = matchedPattern == 1;
		return true;
	}

	@Override
	protected Number @Nullable [] get(Event event) {
		Number number = angle.getSingle(event);

		if (number == null)
			return null;

		if (isRadians) {
			return new Double[]{Math.toDegrees(number.doubleValue())};
		}

		if (number instanceof Integer integer) {
			return new Integer[]{integer};
		} else if (number instanceof Long lng) {
			return new Long[]{lng};
		} else if (number instanceof Double dbl) {
			return new Double[]{dbl};
		}

		return new Float[]{number.floatValue()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return angle.toString(event, debug) + (isRadians ? " degrees" : " radians");
	}

}
