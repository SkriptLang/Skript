package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.registrations.EventValues.TIME_FUTURE;
import static ch.njol.skript.registrations.EventValues.TIME_PAST;

@Name("World")
@Description("The world the event occurred in.")
@Example("world is \"world_nether\"")
@Example("teleport the player to the world's spawn")
@Example("set the weather in the player's world to rain")
@Example("set {_world} to world of event-chunk")
@Since("1.0")
public class ExprWorld extends PropertyExpression<Object, World> {

	static {
		Skript.registerExpression(ExprWorld.class, World.class, ExpressionType.PROPERTY, "[the] world [of %locations/entities/chunk%]", "%locations/entities/chunk%'[s] world");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		setExpr(exprs[0]);
		return true;
	}
	
	@Override
	protected World[] get(Event event, Object[] source) {
		return get(source, objInWorld -> {
			// if getTime is not 0, we know:
			// - Not delayed
			// - In a PlayerTeleportEvent
			// - the source expr was the event-value
			// check the event anyway since it casts for us
			if (event instanceof PlayerTeleportEvent playerTeleportEvent) {
				if (getTime() == TIME_FUTURE) {
					// future
					return playerTeleportEvent.getTo().getWorld();
				} else if (getTime() == TIME_PAST) {
					// past
					return playerTeleportEvent.getFrom().getWorld();
				}
			}
			if (objInWorld instanceof Entity entity) {
				return entity.getWorld();
			} else if (objInWorld instanceof Location location) {
				return location.getWorld();
			} else if (objInWorld instanceof Chunk chunk) {
				return chunk.getWorld();
			}
			assert false : objInWorld;
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && getExpr().canReturn(Location.class))
			return CollectionUtils.array(World.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;

		// TODO: this is suspicious - test if it works in all cases + whether we should have a Location changer instead.
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Location location) {
				location.setWorld((World) delta[0]);
			}
		}
	}

	@Override
	public boolean setTime(int time) {
		return super.setTime(time, getExpr(), PlayerTeleportEvent.class);
	}

	@Override
	public Class<World> getReturnType() {
		return World.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the world" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(event, debug));
	}

}
