package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Pathfind")
@Description({"Make an entity pathfind towards a location or another entity. Not all entities can pathfind. " +
	"If the pathfinding target is another entity, the entities may or may not continuously follow the target."})
@Examples({
	"make all creepers pathfind towards player",
	"make all cows stop pathfinding",
	"make event-entity pathfind towards player at speed 1"
})
@Since("2.7")
@RequiredPlugins("Paper")
public class EffPathfind extends Effect {

	static {
		if (Skript.classExists("org.bukkit.entity.Mob") && Skript.methodExists(Mob.class, "getPathfinder"))
			Skript.registerEffect(EffPathfind.class,
				"make %livingentities% (pathfind|move) to[wards] %livingentity/location% [at speed %-number%]",
				"make %livingentities% stop (pathfinding|moving)");
	}

	private Expression<LivingEntity> entities;

	@Nullable
	private Expression<Number> speed;

	@Nullable
	private Expression<?> target;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		target = matchedPattern == 0 ? exprs[1] : null;
		speed = matchedPattern == 0 ? (Expression<Number>) exprs[2] : null;
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object target = this.target != null ? this.target.getSingle(event) : null;
		double speed = this.speed != null ? this.speed.getOptionalSingle(event).orElse(1).doubleValue() : 1;
		for (LivingEntity entity : entities.getArray(event)) {
			if (!(entity instanceof Mob))
				continue;
			if (target instanceof LivingEntity) {
				((Mob) entity).getPathfinder().moveTo((LivingEntity) target, speed);
			} else if (target instanceof Location) {
				((Mob) entity).getPathfinder().moveTo((Location) target, speed);
			} else if (this.target == null) {
				((Mob) entity).getPathfinder().stopPathfinding();
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (target == null)
			return "make " + entities.toString(event, debug) + " stop pathfinding";
		String repr = "make " + entities.toString(event, debug) + " pathfind towards " + target.toString(event, debug);
		if (speed != null)
			repr += " at speed " + speed.toString(event, debug);
		return repr;
	}

}
