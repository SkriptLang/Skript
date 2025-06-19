package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@Name("Push")
@Description("Push entities around.")
@Example("push the player upwards")
@Example("push the victim downwards at speed 0.5")
@Example("push player along vector from player to player's target at speed 2")
@Since("1.4.6")
public class EffPush extends Effect {

	static {
		Skript.registerEffect(EffPush.class, "(push|thrust) %entities% [along] %direction% [(at|with) (speed|velocity|force) %-number%]");
	}

	private Expression<Entity> entities;
	private Expression<Direction> direction;
	private @Nullable Expression<Number> speed = null;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		direction = (Expression<Direction>) exprs[1];
		speed = (Expression<Number>) exprs[2];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		Direction direction = this.direction.getSingle(event);
		if (direction == null)
			return;
		Number speed = this.speed != null ? this.speed.getSingle(event) : null;
		if (this.speed != null && speed == null)
			return;
		Entity[] entities = this.entities.getArray(event);
		for (Entity entity : entities) {
			Vector3d pushDirection = direction.getDirection(entity);
			if (speed != null)
				pushDirection.normalize(speed.doubleValue());
			if (!pushDirection.isFinite()) {
				// Some component of the mod vector is not finite, so just stop
				return;
			}
			entity.setVelocity(entity.getVelocity().add(Vector.fromJOML(pushDirection)));
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "push " + entities.toString(event, debug) + " " + direction.toString(event, debug) +
				(speed != null ? " at speed " + speed.toString(event, debug) : "");
	}
	
}
