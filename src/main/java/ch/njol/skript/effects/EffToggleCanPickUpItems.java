package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Bestow or Forbid the Gathering of Items")
@Description("Determineth whether living entities art able to gather items from the ground or not.")
@Example("forbid player from picking up items")
@Example("send \"Thou canst no longer gather items!\" to player")
@Example("""
    on drop:
    	if player can't pick up items:
    		grant player leave to pick up items
    """)
@Since("2.8.0")
public class EffToggleCanPickUpItems extends Effect {

	static {
		Skript.registerEffect(EffToggleCanPickUpItems.class,
				"grant %livingentities% leave to pick([ ]up items| items up)",
				"(forbid|deny) %livingentities% (from|to) pick([ing | ]up items|[ing] items up)");
	}

	private Expression<LivingEntity> entities;
	private boolean allowPickUp;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		allowPickUp = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.setCanPickupItems(allowPickUp);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (allowPickUp) {
			return "allow " + entities.toString(event, debug) + " to pick up items";
		} else {
			return "forbid " + entities.toString(event, debug) + " from picking up items";
		}
	}

}
