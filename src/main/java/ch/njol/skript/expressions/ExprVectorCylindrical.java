package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Vectors - Cylindrical Shape")
@Description("Forms a 'cylindrical shaped' vector using yaw to manipulate the current point.")
@Examples({
	"loop 360 times:",
		"\tset {_v} to cylindrical vector radius 1, yaw loop-value, height 2",
	"set {_v} to cylindrical vector radius 1, yaw 90, height 2"
})
@Since("2.2-dev28")
public class ExprVectorCylindrical extends SimpleExpression<Vector> {

	private static final double DEG_TO_RAD = Math.PI / 180;

	static {
		Skript.registerExpression(ExprVectorCylindrical.class, Vector.class, ExpressionType.SIMPLE,
				"[a] [new] cylindrical vector [from|with] [radius] %number%, [yaw] %number%(,[ and]| and) [height] %number%");
	}

	@SuppressWarnings("null")
	private Expression<Number> radius, yaw, height;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		radius = (Expression<Number>) exprs[0];
		yaw = (Expression<Number>) exprs[1];
		height = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Number radius = this.radius.getSingle(event);
		Number yaw = this.yaw.getSingle(event);
		Number height = this.height.getSingle(event);
		if (radius == null || yaw == null || height == null)
			return null;
		return CollectionUtils.array(fromCylindricalCoordinates(radius.doubleValue(), ExprYawPitch.fromSkriptYaw(yaw.floatValue()), height.doubleValue()));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public Expression<? extends Vector> simplify() {
		if (radius instanceof Literal<Number> && yaw instanceof Literal<Number> && height instanceof Literal<Number>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cylindrical vector with radius " + radius.toString(event, debug) + ", yaw " +
				yaw.toString(event, debug) + " and height " + height.toString(event, debug);
	}

	public static Vector fromCylindricalCoordinates(double radius, double phi, double height) {
		double r = Math.abs(radius);
		double p = phi * DEG_TO_RAD;
		double x = r * Math.cos(p);
		double z = r * Math.sin(p);
		return new Vector(x, height, z);
	}

}
