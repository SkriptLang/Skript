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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Knockback")
@Description("Apply the same velocity as a knockback to living entities in a direction. " +
	"Mechanics such as knockback resistance will be factored in.")
@Example("knockback player north")
@Example("knock victim (vector from attacker to victim) with strength 10")
@Since("2.7")
public class EffKnockback extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffKnockback.class,
			"(apply knockback to|knock[back]) %livingentities% [%direction%] [with (strength|force) %-number%]"
		);
	}

	private Expression<LivingEntity> entities;
	private Expression<Direction> direction;
	@Nullable
	private Expression<Number> strength;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		// noinspection unchecked
		direction = (Expression<Direction>) expressions[1];
		// noinspection unchecked
		strength = (Expression<Number>) expressions[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Direction direction = this.direction.getSingle(event);
		if (direction == null)
			return;

		double strength = this.strength != null ? this.strength.getOptionalSingle(event).orElse(1).doubleValue() : 1.0;

		if (strength <= 0) {
			error("Knockback strength must be larger than 0");
			return;
		}

		for (LivingEntity livingEntity : entities.getArray(event)) {
			Vector directionVector = direction.getDirection(livingEntity);
			// Flip the direction, because LivingEntity#knockback() takes the direction of the source of the knockback,
			// not the direction of the actual knockback.
			directionVector.multiply(-1);
			livingEntity.knockback(strength, directionVector.getX(), directionVector.getZ());
			// ensure velocity is sent to client
			livingEntity.setVelocity(livingEntity.getVelocity());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "knockback " + entities.toString(event, debug) + " " + direction.toString(event, debug) +
			" with strength " + (strength != null ? strength.toString(event, debug) : "1");
	}

}
