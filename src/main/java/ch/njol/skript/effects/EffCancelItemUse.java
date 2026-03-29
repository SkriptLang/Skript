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

@Name("Halt Active Item")
@Description({
	"Interrupteth the action entities may be endeavouring to complete.",
	"For instance, halting the consumption of victuals, or the drawing of a bow."
})
@Example("""
    on damage of player:
    	if the victim's active tool is a bow:
    		halt the employment of the player's active item
    """)
@Since("2.8.0")
public class EffCancelItemUse extends Effect {

	static {
		// TODO - remove this when Spigot support is dropped
		if (Skript.methodExists(LivingEntity.class, "clearActiveItem"))
			Skript.registerEffect(EffCancelItemUse.class,
					"(halt|interrupt) [the] employ[ment] of %livingentities%'[s] [active|current] item"
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
