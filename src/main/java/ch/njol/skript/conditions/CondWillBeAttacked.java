package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Will Be Attacked")
@Description("""
	Checks if the entity in <a href='#Evt'>Player Pre Attack Entity Event</a> will be attacked.
	Returns false for non-living entities. However, the event won't be called for them unless explicitly listened to.
	""")
@Example("""
	on player pre attack entity:
		if victim will be attacked:
			cancel event
	""")
@Since("INSERT VERSION")
public class CondWillBeAttacked extends Condition {

	static {
		Skript.registerCondition(CondWillBeAttacked.class,
				"[the] [event-](entity|victim) (:will|will not|won't) be attacked"
		);
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(!parseResult.hasTag("will"));
		return true;
	}

	@Override
	public boolean check(Event e) {
		return (e instanceof PrePlayerAttackEntityEvent && ((PrePlayerAttackEntityEvent) e).willAttack()) ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the entity " + (isNegated() ? "will not be attacked" : "will be attacked");
	}

}
