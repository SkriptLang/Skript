package ch.njol.skript.expressions;

import org.bukkit.util.Vector;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Vectors - Squared Length")
@Description("Gets the squared length of a vector.")
@Examples("send \"%squared length of vector 1, 2, 3%\"")
@Since("2.2-dev28")
public class ExprVectorSquaredLength extends SimplePropertyExpression<Vector, Number> {

	static {
		register(ExprVectorSquaredLength.class, Number.class, "squared length[s]", "vectors");
	}

	@SuppressWarnings("unused")
	@Override
	public Number convert(Vector vector) {
		return vector.lengthSquared();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "squared length of vector";
	}


}
