package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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

@Name("Push")
@Description("Push entities around.")
@Examples({
	"push the player upwards",
	"push the victim downwards at speed 0.5"
})
@Since("1.4.6")
public class EffPush extends Effect {

	static {
		Skript.registerEffect(EffPush.class, "(push|thrust) %entities% %direction% [(at|with) (speed|velocity|force) %-number%]");
	}

	private Expression<Entity> entities;
	private Expression<Direction> direction;
	private @Nullable Expression<Number> speed = null;

	@Override
	@SuppressWarnings("unchecked")
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

		Number velocity = speed != null ? speed.getSingle(event) : null;
		if (speed != null && velocity == null)
			return;

		Entity[] entities = this.entities.getArray(event);
		for (Entity entity : entities) {
			Vector mod = direction.getDirection(entity);
			if (velocity != null)
				mod.normalize().multiply(velocity.doubleValue());
			if (!(Double.isFinite(mod.getX()) && Double.isFinite(mod.getY()) && Double.isFinite(mod.getZ()))) {
				// Some component of the mod vector is not finite, so just stop
				return;
			}
			entity.setVelocity(entity.getVelocity().add(mod)); // REMIND add NoCheatPlus exception to players
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "push " + entities.toString(event, debug) + " " + direction.toString(event, debug) + (speed != null ? " at speed " + speed.toString(event, debug) : "");
	}
	
}
