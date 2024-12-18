package ch.njol.skript.expressions;

import ch.njol.util.VectorMath;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Vectors - Length")
@Description("Gets or sets the length of a vector.")
@Examples({
	"send \"%standard length of vector 1, 2, 3%\"",
	"set {_v} to vector 1, 2, 3",
	"set standard length of {_v} to 2",
	"send \"%standard length of {_v}%\""
})
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
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert delta != null;
		final Vector[] vectors = getExpr().getArray(event);
		double deltaLength = ((Number) delta[0]).doubleValue();
		switch (mode) {
			case REMOVE:
				deltaLength = -deltaLength;
				//$FALL-THROUGH$
			case ADD:
				for (Vector vector : vectors) {
					if (VectorMath.isZero(vector) || (deltaLength < 0 && vector.lengthSquared() < deltaLength * deltaLength)) {
						vector.zero();
					} else {
						double newLength = deltaLength + vector.length();
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(newLength);
					}
				}
				break;
			case SET:
				for (Vector vector : vectors) {
					if (deltaLength < 0 || VectorMath.isZero(vector)) {
						vector.zero();
					} else {
						if (!vector.isNormalized())
							vector.normalize();
						vector.multiply(deltaLength);
					}
				}
				break;
		}
		getExpr().change(event, vectors, ChangeMode.SET);
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "vector length";
	}

}
