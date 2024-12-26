package ch.njol.skript.expressions;

import org.bukkit.Location;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Altitude")
@Description("Effectively an alias of 'y-<a href='#ExprCoordinate'>coordinate</a> of …', it represents the height of some object above bedrock.")
@Examples({"on damage:",
		"	altitude of the attacker is higher than the altitude of the victim",
		"	set damage to damage * 1.2"})
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
	
}
