package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PaperEntityUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.LookAnchor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Look At")
@Description("Forces the mob(s) or player(s) to look at an entity, vector or location. Vanilla max head pitches range from 10 to 50.")
@Example("force the player to look towards event-entity's feet")
@Example("""
	on entity explosion:
		set {_player} to the nearest player
		{_player} is set
		distance between {_player} and the event-location is less than 15
		make {_player} look towards vector from the {_player} to location of the event-entity
	""")
@Example("force {_enderman} to face the block 3 meters above {_location} at head rotation speed 100.5 and max head pitch -40")
@Since("2.7")
public class EffLook extends Effect {

	static {
		Skript.registerEffect(EffLook.class,
			"(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) " +
			"%entity%'s (feet:feet|eyes) [(at|with) [head] [rotation] speed %-number%] " +
			"[[and] max[imum] [head] pitch %-number%]",

			"(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) " +
				"[the] (feet:feet|eyes) of %entity% [(at|with) [head] [rotation] speed %-number%] " +
				"[[and] max[imum] [head] pitch %-number%]",

			"(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %vector/location/entity% " +
			"[(at|with) [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]");
	}

	private LookAnchor anchor = LookAnchor.EYES;
	private Expression<LivingEntity> entities;

	@Nullable
	private Expression<Number> speed, maxPitch;

	/**
	 * Can be Vector, Location or an Entity.
	 */
	private Expression<?> target;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		target = exprs[1];
		speed = (Expression<Number>) exprs[2];
		maxPitch = (Expression<Number>) exprs[3];
		if (parseResult.hasTag("feet"))
			anchor = LookAnchor.FEET;
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = target.getSingle(event);
		if (object == null)
			return;

		Float speed = this.speed == null ? null : this.speed.getOptionalSingle(event).map(Number::floatValue).orElse(null);
		Float maxPitch = this.maxPitch == null ? null : this.maxPitch.getOptionalSingle(event).map(Number::floatValue).orElse(null);

		PaperEntityUtils.lookAt(anchor, object, speed, maxPitch, entities.getArray(event));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "force " + entities.toString(event, debug) + " to look at " + target.toString(event, debug);
	}

}
