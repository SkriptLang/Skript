package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;

@Name("Middle of Location")
@Description("Returns the middle/center of a location. In other words, returns the middle of the X, Z coordinates and the floor value of the Y coordinate of a location.")
@Example("""
	command /stuck:
		executable by: players
		trigger:
			teleport player to the center of player's location
			send "You're no longer stuck."
	""")
@Since("2.6.1")
public class ExprMiddleOfLocation extends SimplePropertyExpression<Location, Location> {
	
	static {
		register(ExprMiddleOfLocation.class, Location.class, "(middle|center) [point]", "location");
	}
	
	@Override
	@Nullable
	public Location convert(Location loc) {
		return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5);
	}
	
	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public Expression<? extends Location> simplify() {
		if (getExpr() instanceof Literal<? extends Location>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	protected String getPropertyName() {
		return "middle point";
	}
	
}
