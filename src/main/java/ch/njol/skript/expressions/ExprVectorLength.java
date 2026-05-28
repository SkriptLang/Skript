package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

import java.util.function.Function;

@Name("Vectors - Length")
@Description("Gets or sets the length of a vector.")
@Example("send \"%standard length of vector 1, 2, 3%\"")
@Example("set {_v} to vector 1, 2, 3")
@Example("set standard length of {_v} to 2")
@Example("send \"%standard length of {_v}%\"")
@Since("2.2-dev28")
public class ExprVectorLength extends SimplePropertyExpression<Vector, Number> {

	static {
		register(ExprVectorLength.class, Number.class, "(vector|standard|normal) length[s]", "vectors");
	}

	@Override
	@SuppressWarnings("unused")
	public Number convert(Vector vector) {
		return vector.length();
	}

	@Override
	@SuppressWarnings("null")
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		double deltaLength = ((Number) delta[0]).doubleValue();

		Function<Vector, Vector> changeFunction;
		switch (mode) {
			case REMOVE:
				deltaLength = -deltaLength;
				//$FALL-THROUGH$
			case ADD:
				final double finalDeltaLength = deltaLength;
				final double finalDeltaLengthSquared = deltaLength * deltaLength;
				changeFunction = vector -> {
					if (vector.isZero() || (finalDeltaLength < 0 && vector.lengthSquared() < finalDeltaLengthSquared)) {
						vector.zero();
					} else {
						double newLength = finalDeltaLength + vector.length();
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(newLength);
					}
					return vector;
				};
				break;
			case SET:
				final double finalDeltaLength1 = deltaLength;
				changeFunction = vector -> {
					if (finalDeltaLength1 < 0 || vector.isZero()) {
						vector.zero();
					} else {
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(finalDeltaLength1);
					}
					return vector;
				};
				break;
			default:
				return;
		}

		// noinspection unchecked
		((Expression<Vector>) getExpr()).changeInPlace(event, changeFunction);
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public Expression<? extends Number> simplify() {
		if (getExpr() instanceof Literal<? extends Vector>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "vector length";
	}

}
