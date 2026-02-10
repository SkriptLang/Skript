package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Location with Yaw/Pitch")
@Description("Returns the given locations with the specified yaw and/or pitch.")
@Example("set {_location} to player's location with yaw 0 and pitch 0")
@Since("INSERT VERSION")
public class ExprWithYawPitch extends PropertyExpression<Location, Location> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprWithYawPitch.class, Location.class)
			.supplier(ExprWithYawPitch::new)
			.addPattern("%locations% with [a] (:yaw|:pitch) [of] %number%")
			.addPattern("%locations% with [a] yaw [of] %number% and [a] pitch [of] %number%")
			.build());
	}

	private Expression<Number> yaw, pitch;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Location>) expressions[0]);
		if (parseResult.hasTag("yaw")) {
			yaw = (Expression<Number>) expressions[1];
		} else if (parseResult.hasTag("pitch")) {
			pitch = (Expression<Number>) expressions[1];
		} else {
			yaw = (Expression<Number>) expressions[1];
			pitch = (Expression<Number>) expressions[2];
		}
		return true;
	}

	@Override
	protected Location[] get(Event event, Location[] source) {
		Number yaw = this.yaw != null ? this.yaw.getSingle(event) : null;
		Number pitch = this.pitch != null ? this.pitch.getSingle(event) : null;
		return get(source, location -> {
			float finalYaw = yaw != null ? yaw.floatValue() : location.getYaw();
			float finalPitch = pitch != null ? pitch.floatValue() : location.getPitch();
			Location clone = location.clone();
			clone.setYaw(finalYaw);
			clone.setPitch(finalPitch);
			return clone;
		});
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(getExpr(), "with")
			.appendIf(yaw != null, "yaw", yaw)
			.appendIf(yaw != null && pitch != null, "and")
			.appendIf(pitch != null, "pitch", pitch)
			.toString();
	}

}
