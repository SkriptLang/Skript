package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

@Name("Location")
@Description("The location where an event happened (e.g. at an entity or block), or a location <a href='#ExprDirection'>relative</a> to another (e.g. 1 meter above another location).")
@Examples({
	"drop 5 apples at the event-location # exactly the same as writing 'drop 5 apples'",
	"set {_loc} to the location 1 meter above the player"
})
@Since("2.0")
public class ExprLocation extends WrapperExpression<Location> {

	static {
		Skript.registerExpression(ExprLocation.class, Location.class, ExpressionType.SIMPLE, "[the] [event-](location|position)");
		Skript.registerExpression(ExprLocation.class, Location.class, ExpressionType.COMBINED, "[the] (location|position) %directions% [%location%]");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs.length > 0) {
			super.setExpr(Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]));
			return true;
		} else {
			setExpr(new EventValueExpression<>(Location.class));
			return ((EventValueExpression<Location>) getExpr()).init(matchedPattern, isDelayed, parseResult);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr() instanceof EventValueExpression ? "location" : "location " + getExpr().toString(event, debug);
	}

}
