package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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

/**
 * @author Peter Güttinger
 */
@Name("Location")
@Description({"The location of a block, entity or offline player. This not only represents the x, y and z coordinates of the location but also includes the world and the direction an entity is looking " +
		"(e.g. teleporting to a saved location will make the teleported entity face the same saved direction every time).",
		"Please note that the location of an entity is at it's feet, use <a href='#ExprEyeLocation'>head location</a> to get the location of the head."})
@Examples({"set {home::%uuid of player%} to the location of the player",
		"message \"You home was set to %player's location% in %player's world%.\"",
		"set {_loc} to location of offline player \"Notch\""})
@Since("1.0, INSERT (offline players)")
public class ExprLocationOf extends SimpleExpression<Location> {
	static {
		Skript.registerExpression(ExprLocationOf.class, Location.class, ExpressionType.PROPERTY,
			"(location|position) of %location/offlineplayers%",
			"%location/offlineplayers%'[s] (location|position)");
	}

	private Expression<?> expr;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		expr = exprs[0];
		return true;
	}
	
	@Override
	protected @Nullable Location[] get(Event e) {
		Object[] values = expr.getArray(e);
		if (values.length == 0)
			return new Location[0];

		Location[] locations = new Location[values.length];
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];
			if (value instanceof Location) {
				locations[i] = (Location) value;
			} else if (value instanceof OfflinePlayer) {
				locations[i] = ((OfflinePlayer) value).getLocation();
			}
		}
		return locations;
	}

	@Override
	public boolean isSingle() {
		return expr.isSingle();
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the location of " + expr.toString(e, debug);
	}
	
}
