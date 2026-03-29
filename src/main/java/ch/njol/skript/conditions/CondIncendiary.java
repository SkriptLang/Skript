package ch.njol.skript.conditions;

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
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Be Incendiary")
@Description("Discerneth whether an entity shall kindle fire when it doth explode. This condition may also be employed within an explosion prime event.")
@Example("""
    on explosion prime:
    	if the explosion is fiery:
    		broadcast "A fiery explosive hath been set alight!"
    """)
@Since("2.5")
public class CondIncendiary extends Condition {

	static {
		Skript.registerCondition(CondIncendiary.class,
				"%entities% ((is|are) incendiary|bringeth[s] a[n] (incendiary|fiery) explosion)",
				"%entities% ((is not|are not|isn't|aren't) incendiary|(does not|do not|doesn't|don't) bringeth[s] a[n] (incendiary|fiery) explosion)",
				"the [event(-| )]explosion (is|1¦(is not|isn't)) (incendiary|fiery)"
		);
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	private boolean isEvent;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isEvent = matchedPattern == 2;
		if (isEvent && !getParser().isCurrentEvent(ExplosionPrimeEvent.class)) {
			Skript.error("Checking if 'the explosion' is fiery is only possible in an explosion prime event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		if (!isEvent)
			entities = (Expression<Entity>) exprs[0];
		setNegated(matchedPattern == 1 || parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		if (isEvent) {
			if (!(e instanceof ExplosionPrimeEvent))
				return isNegated();

			return ((ExplosionPrimeEvent) e).getFire() ^ isNegated();
		}
		return entities.check(e, entity -> entity instanceof Explosive && ((Explosive) entity).isIncendiary(), isNegated());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (isEvent)
			return "the event-explosion " + (isNegated() == false ? "is" : "is not") + " incendiary";
		if (entities.isSingle())
			return entities.toString(e, debug) + (isNegated() == false ? " is" : " is not") + " incendiary";
		return entities.toString(e, debug) + (isNegated() == false ? " are" : " are not") + " incendiary";
	}

}
