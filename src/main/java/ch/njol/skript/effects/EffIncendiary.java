package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Render Incendiary")
@Description("Declareth whether an entity's explosion shall leave fire in its wake. This effect may also be employ'd within an explosion prime event.")
@Example("""
    on explosion prime:
    	render the explosion fiery
    """)
@Since("2.5")
public class EffIncendiary extends Effect {

	static {
		Skript.registerEffect(EffIncendiary.class,
				"render %entities% [(1¦not)] incendiary",
				"render %entities%'[s] explosion [(1¦not)] (incendiary|fiery)",
				"render [the] [event(-| )]explosion [(1¦not)] (incendiary|fiery)"
		);
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	private boolean causeFire;

	private boolean isEvent;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = matchedPattern == 2;
		if (isEvent && !getParser().isCurrentEvent(ExplosionPrimeEvent.class)) {
			Skript.error("Making 'the explosion' fiery is only usable in an explosion prime event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (!isEvent)
			entities = (Expression<Entity>) exprs[0];
		causeFire = parseResult.mark != 1;
		return true;
	}

	@Override
	protected void execute(Event e) {
		if (isEvent) {
			if (!(e instanceof ExplosionPrimeEvent))
				return;

			((ExplosionPrimeEvent) e).setFire(causeFire);
		} else {
			for (Entity entity : entities.getArray(e)) {
				if (entity instanceof Explosive)
					((Explosive) entity).setIsIncendiary(causeFire);
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (isEvent)
			return "make the event-explosion " + (causeFire == true ? "" : "not") + " fiery";
		return "make " + entities.toString(e, debug) + (causeFire == true ? "" : " not") + " incendiary";
	}

}
