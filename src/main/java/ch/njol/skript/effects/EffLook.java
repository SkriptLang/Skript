package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PaperEntityUtils;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.LookAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Look At")
@Description({
	"Forces the mob(s) or player(s) to look at an entity, vector or location.",
	"Vanilla max head pitches range from 10 to 50."
})
@Examples({
	"force the player to look towards event-entity's feet",
	"",
	"on entity explosion:",
		"\tset {_player} to the nearest player",
		"\t{_player} is set",
		"\tdistance between {_player} and the event-location is less than 15",
		"\tmake {_player} look towards vector from the {_player} to location of the event-entity",
	"",
	"force {_enderman} to face the block 3 meters above {_location} at head rotation speed 100.5 and max head pitch -40"
})
@Since("2.7")
@RequiredPlugins("Paper 1.17+, Paper 1.19.1+ (Players & Look Anchors)")
public class EffLook extends Effect implements SyntaxRuntimeErrorProducer {

	private static final boolean LOOK_ANCHORS = Skript.classExists("io.papermc.paper.entity.LookAnchor");

	static {
		if (Skript.methodExists(Mob.class, "lookAt", Entity.class)) {
			if (LOOK_ANCHORS) {
				Skript.registerEffect(EffLook.class, "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) " +
					"(%entity%['s (feet:feet|eyes)]|of:(feet:feet|eyes) of %entity%) " +
					"[at [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]",

					"(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %vector/location% " +
					"[at [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]");
			} else {
				Skript.registerEffect(EffLook.class, "(force|make) %livingentities% [to] (face [towards]|look [(at|towards)]) %vector/location/entity% " +
					"[at [head] [rotation] speed %-number%] [[and] max[imum] [head] pitch %-number%]");
			}
		}
	}

	private Node node;
	private LookAnchor anchor = LookAnchor.EYES;
	private Expression<LivingEntity> entities;
	private Expression<?> target; // can be a vector, location, or entity
	private @Nullable Expression<Number> speed, maxPitch;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		entities = (Expression<LivingEntity>) exprs[0];
		if (LOOK_ANCHORS && matchedPattern == 0) {
			target = exprs[parseResult.hasTag("of") ? 2 : 1];
			speed = (Expression<Number>) exprs[3];
			maxPitch = (Expression<Number>) exprs[4];
			if (parseResult.hasTag("feet"))
				anchor = LookAnchor.FEET;
		} else {
			target = exprs[1];
			speed = (Expression<Number>) exprs[2];
			maxPitch = (Expression<Number>) exprs[3];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = target.getSingle(event);
		if (object == null) {
			error("The provided target was not set.", target.toString());
			return;
		}

		Float speed = null;
		if (this.speed != null) {
			Number provided = this.speed.getSingle(event);
			if (provided == null) {
				warning("The provided speed was not set.", this.speed.toString());
			} else {
				speed = provided.floatValue();
			}
		}

		Float maxPitch = null;
		if (this.maxPitch != null) {
			Number provided = this.maxPitch.getSingle(event);
			if (provided == null) {
				warning("The provided max pitch was not set.", this.maxPitch.toString());
			} else {
				maxPitch = provided.floatValue();
			}
		}

		if (LOOK_ANCHORS) {
			PaperEntityUtils.lookAt(anchor, object, speed, maxPitch, entities.getArray(event));
		} else {
			PaperEntityUtils.lookAt(object, speed, maxPitch, entities.getArray(event));
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "force " + entities.toString(event, debug) + " to look at " + target.toString(event, debug);
	}

}
