package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.function.Consumer;

@Name("Pathfind")
@Description("""
	"Make an entity pathfind towards a location or another entity. Not all entities can pathfind. \
	If the pathfinding target is another entity, the entities may or may not continuously follow the target.
	""")
@Example("make all creepers pathfind towards player")
@Example("make all cows stop pathfinding")
@Example("make event-entity pathfind towards player at speed 1")
@Since("2.7")
public class EffPathfind extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffPathfind.class)
				.addPatterns(
					"make %livingentities% (pathfind|move) to[wards] %livingentity/location% [at speed %-number%]",
					"make %livingentities% stop (pathfinding|moving)"
				).supplier(EffPathfind::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private @Nullable Expression<Number> speed;
	private @Nullable Expression<?> target;

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
		Consumer<Mob> consumer;
		if (target instanceof LivingEntity entity) {
			consumer = mob -> mob.getPathfinder().moveTo(entity, speed);
		} else if (target instanceof Location location) {
			consumer = mob -> mob.getPathfinder().moveTo(location, speed);
		} else {
			consumer = mob -> mob.getPathfinder().stopPathfinding();
		}
		for (LivingEntity entity : entities.getArray(event)) {
			if (!(entity instanceof Mob mob))
				continue;
			consumer.accept(mob);
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
