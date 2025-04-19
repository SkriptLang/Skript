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

import static ch.njol.skript.expressions.ExprYawPitch.fromYawAndPitch;

@Name("Vectors - Vector from Yaw and Pitch")
@Description("Creates a vector from a yaw and pitch value.")
@Examples("set {_v} to vector from yaw 45 and pitch 45")
@Since("2.2-dev28")
public class ExprVectorFromYawAndPitch extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorFromYawAndPitch.class, Vector.class, ExpressionType.COMBINED,
			"[a] [new] vector (from|with) yaw %number% and pitch %number%",
			"[a] [new] vector (from|with) pitch %number% and yaw %number%");
	}

	private Expression<Number> pitch, yaw;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pitch = (Expression<Number>) exprs[matchedPattern ^ 1];
		yaw = (Expression<Number>) exprs[matchedPattern];
		return true;
	}

	@Override
	protected Vector[] get(Event event) {
		Number skriptYaw = yaw.getSingle(event);
		Number skriptPitch = pitch.getSingle(event);
		if (skriptYaw == null || skriptPitch == null)
			return null;
		float yaw = ExprYawPitch.fromSkriptYaw(wrapAngleDeg(skriptYaw.floatValue()));
		float pitch = ExprYawPitch.fromSkriptPitch(wrapAngleDeg(skriptPitch.floatValue()));
		return CollectionUtils.array(fromYawAndPitch(yaw, pitch));
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
		return "vector from yaw " + yaw.toString(event, debug) + " and pitch " + pitch.toString(event, debug);
	}

	public static float wrapAngleDeg(float angle) {
		angle %= 360f;
		if (angle <= -180) {
			return angle + 360;
		} else if (angle > 180) {
			return angle - 360;
		} else {
			return angle;
		}
	}

}
