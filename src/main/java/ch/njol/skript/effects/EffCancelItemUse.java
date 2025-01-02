package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Cancel Active Item")
@Description({
	"Interrupts the action entities may be trying to complete.",
	"For example, interrupting eating, or drawing back a bow."
})
@Examples({
	"on damage of player:",
		"\tif the victim's active tool is a bow:",
			"\t\tinterrupt the usage of the player's active item"
})
@Since("2.8.0")
@RequiredPlugins("Paper 1.16+")
public class EffCancelItemUse extends Effect {

	static {
		if (Skript.methodExists(LivingEntity.class, "clearActiveItem"))
			Skript.registerEffect(EffCancelItemUse.class,
					"(cancel|interrupt) [the] us[ag]e of %livingentities%'[s] [active|current] item"
			);
	}

	private Expression<LivingEntity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.clearActiveItem();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "cancel the usage of " + entities.toString(event, debug) + "'s active item";
	}

}
