package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import org.bukkit.Location;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Altitude")
@Description("Effectively an alias of 'y-<a href='#ExprCoordinate'>coordinate</a> of …', it represents the height of some object above bedrock.")
@Example("""
	on damage:
		altitude of the attacker is higher than the altitude of the victim
		set damage to damage * 1.2
	""")
@Since("1.4.3")
public class ExprAltitude extends SimplePropertyExpression<Location, Number> {
	
	static {
		register(ExprAltitude.class, Number.class, "altitude[s]", "locations");
	}

	@Override
	public Number convert(final Location l) {
		return l.getY();
	}
	
	@Override
	protected String getPropertyName() {
		return "altitude";
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public Expression<? extends Number> simplify() {
		// as of INSERT VERSION, there are no literal locations but this is implemented for when pure functions can
		// be simplified.
		if (getExpr() instanceof Literal<? extends Location>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

}
