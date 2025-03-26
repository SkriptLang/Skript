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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Vectors - Spherical Shape")
@Description("Forms a 'spherical shaped' vector using yaw and pitch to manipulate the current point.")
@Examples({
	"loop 360 times:",
		"\tset {_v} to spherical vector radius 1, yaw loop-value, pitch loop-value",
	"set {_v} to spherical vector radius 1, yaw 45, pitch 90"
})
@Since("2.2-dev28")
public class ExprVectorSpherical extends SimpleExpression<Vector> {

	private static final double DEG_TO_RAD = Math.PI / 180;

	static {
		Skript.registerExpression(ExprVectorSpherical.class, Vector.class, ExpressionType.SIMPLE,
				"[a] [new] spherical vector [(from|with)] [radius] %number%, [yaw] %number%(,[ and]| and) [pitch] %number%");
	}

	@SuppressWarnings("null")
	private Expression<Number> radius, yaw, pitch;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		radius = (Expression<Number>) exprs[0];
		yaw = (Expression<Number>) exprs[1];
		pitch = (Expression<Number>) exprs[2];
		return true;
	}

	@Override
	@SuppressWarnings("null")
	protected Vector[] get(Event event) {
		Number radius = this.radius.getSingle(event);
		Number yaw = this.yaw.getSingle(event);
		Number pitch = this.pitch.getSingle(event);
		if (radius == null || yaw == null || pitch == null)
			return null;
		return CollectionUtils.array(fromSphericalCoordinates(radius.doubleValue(),
			ExprYawPitch.fromSkriptYaw(yaw.floatValue()), pitch.floatValue() + 90));
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
	public String toString(@Nullable Event event, boolean debug) {
		return "spherical vector with radius " + radius.toString(event, debug) + ", yaw " + yaw.toString(event, debug) +
				" and pitch" + pitch.toString(event, debug);
	}

	public static Vector fromSphericalCoordinates(double radius, double theta, double phi) {
		double r = Math.abs(radius);
		double t = theta * DEG_TO_RAD;
		double p = phi * DEG_TO_RAD;
		double sinp = Math.sin(p);
		double x = r * sinp * Math.cos(t);
		double y = r * Math.cos(p);
		double z = r * sinp * Math.sin(t);
		return new Vector(x, y, z);
	}

}
