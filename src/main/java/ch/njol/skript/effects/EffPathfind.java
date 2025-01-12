package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Pathfind")
@Description({
	"Make an entity pathfind towards a location or another entity. Not all entities can pathfind.",
	"If the pathfinding target is another entity, the entities may or may not continuously follow the target."
})
@Examples({
	"make all creepers pathfind towards player",
	"make all cows stop pathfinding",
	"make event-entity pathfind towards player at speed 1"
})
@Since("2.7")
@RequiredPlugins("Paper")
public class EffPathfind extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		if (Skript.classExists("org.bukkit.entity.Mob") && Skript.methodExists(Mob.class, "getPathfinder"))
			Skript.registerEffect(EffPathfind.class,
				"make %livingentities% (pathfind|move) to[wards] %livingentity/location% [at speed %-number%]",
				"make %livingentities% stop (pathfinding|moving)");
	}

	private Node node;
	private Expression<LivingEntity> entities;
	private @Nullable Expression<Number> speed;
	private @Nullable Expression<?> target;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		entities = (Expression<LivingEntity>) exprs[0];
		target = matchedPattern == 0 ? exprs[1] : null;
		speed = matchedPattern == 0 ? (Expression<Number>) exprs[2] : null;
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object target = null;
		if (this.target != null) {
			target = this.target.getSingle(event);
			if (target == null)
				warning("The provided target was not set, so defaulted to stop pathfinding.", this.target.toString());
		}

		double speed = 1;
		if (this.speed != null) {
			Number postSpeed = this.speed.getSingle(event);
			if (postSpeed == null)
				warning("The provided speed was not set, so defaulted to 1.", this.speed.toString());
		}

		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Mob mob) {
				if (target instanceof LivingEntity livingEntity) {
					mob.getPathfinder().moveTo(livingEntity, speed);
				} else if (target instanceof Location location) {
					mob.getPathfinder().moveTo(location, speed);
				} else if (this.target == null) {
					mob.getPathfinder().stopPathfinding();
				}
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
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
