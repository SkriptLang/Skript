package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Despawn")
@Description({
	"Make a living entity despawn when the chunk they're located at is unloaded.",
	"Setting a custom name on a living entity automatically makes it not despawnable.",
	"More information on what and when entities despawn can be found at "
		+ "<a href=\"https://minecraft.wiki/w/Mob_spawning#Despawning\">reference</a>."
})
@Examples({
	"make all entities not despawnable on chunk unload",
	"spawn zombie at location(0, 0, 0):",
		"\tforce event-entity to not despawn when far away",
})
@Since("INSERT VERSION")
public class EffEntityUnload extends Effect {

	static {
		Skript.registerEffect(EffEntityUnload.class,
			"make %livingentities% despawn[able] (on chunk unload|when far away)",
			"force %livingentities% to despawn (on chunk unload|when far away)",
			"prevent %livingentities% from despawning [on chunk unload|when far away]");
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
