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

@Name("Entity Perishment")
@Description({
	"Bid a living entity perish when the chunk wherein it doth dwell is unloaded.",
	"Bestowing a custom name upon a living entity doth automatically render it imperishable.",
	"Further knowledge on what and when entities perish may be found at "
		+ "<a href=\"https://minecraft.wiki/w/Mob_spawning#Despawning\">this tome of reference</a>."
})
@Example("forbid all entities from perishing on chunk unload")
@Example("""
    spawn zombie at location(0, 0, 0):
    	compel event-entity to not perish when far away
    """)
@Since("2.11")
public class EffEntityUnload extends Effect {

	static {
		Skript.registerEffect(EffEntityUnload.class,
			"bid %livingentities% perish[able] (on chunk unload|when far away)",
			"compel %livingentities% to perish (on chunk unload|when far away)",
			"forbid %livingentities% from perishing [on chunk unload|when far away]");
	}

	private Expression<LivingEntity> entities;
	private boolean despawn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		despawn = matchedPattern != 2;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			entity.setRemoveWhenFarAway(despawn);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (despawn)
			return "make " + entities.toString(event, debug) + " despawn on chunk unload";
		return "prevent " + entities.toString(event, debug) + " from despawning on chunk unload";
	}

}
