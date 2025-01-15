package ch.njol.skript.expressions;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("World")
@Description("The world the event occurred in.")
@Examples({
	"world is \"world_nether\"",
	"teleport the player to the world's spawn",
	"set the weather in the player's world to rain",
	"set {_world} to world of event-chunk"
})
@Since("1.0")
public class ExprWorld extends PropertyExpression<Object, World> {

	static {
		Skript.registerExpression(ExprWorld.class, World.class, ExpressionType.PROPERTY, "[the] world [of %locations/entities/chunk%]", "%locations/entities/chunk%'[s] world");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		Expression<?> expr = exprs[0];
		if (expr == null) {
			expr = new EventValueExpression<>(World.class);
			if (!((EventValueExpression<?>) expr).init(matchedPattern, isDelayed, parser))
				return false;
		}
		setExpr(expr);
		return true;
	}

	@Override
	protected World[] get(Event event, Object[] source) {
		if (source instanceof World[] worlds) // event value (see init)
			return worlds;
		return get(source, obj -> {
			if (obj instanceof Entity entity) {
				if (getTime() > 0 && event instanceof PlayerTeleportEvent playerTeleportEvent && obj.equals(playerTeleportEvent.getPlayer()) && !Delay.isDelayed(event))
					return playerTeleportEvent.getTo().getWorld();
				else
					return entity.getWorld();
			} else if (obj instanceof Location location) {
				return location.getWorld();
			} else if (obj instanceof Chunk chunk) {
				return chunk.getWorld();
			}
			assert false : obj;
			return null;
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET ? CollectionUtils.array(World.class) : null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;

		World world = (World) delta[0];
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Location location) {
				location.setWorld(world);
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
		return "world" + (getExpr().isDefault() ? "" : " of " + getExpr().toString(event, debug));
	}

}
