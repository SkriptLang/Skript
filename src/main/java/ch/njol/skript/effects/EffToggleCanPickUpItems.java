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

@Name("Toggle Picking Up Items")
@Description("Determines whether living entities are able to pick up items or not")
@Example("forbid player from picking up items")
@Example("send \"You can no longer pick up items!\" to player")
@Example("""
	on drop:
		if player can't pick up items:
			allow player to pick up items
	""")
@Since("2.8.0")
public class EffToggleCanPickUpItems extends Effect {

	static {
		Skript.registerEffect(EffToggleCanPickUpItems.class,
				"allow %livingentities% to pick([ ]up items| items up)",
				"(forbid|disallow) %livingentities% (from|to) pick([ing | ]up items|[ing] items up)");
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
