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
	Checks whether or not the entity in <a href='#Evt'>Player Pre Attack Entity Event</a> will be attacked.
	Note: The event will not fire for non-living entities if 'listen to cancelled events by default' is not enabled in Skript's config.
	This condition will always return false for non-living entities. Or since the event is canceled, it wouldn't ever be called unless enabled in config.
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
				"[the] (entity|victim) (:will|will not|won't) be attack(ed|ing)"
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
