package ch.njol.skript.effects;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.Leashable;
import ch.njol.util.Kleenean;

@Name("Leash entities")
@Description({
	"Leash entities to other entities, or unleash them.",
	"Leashable entities include wolves, cats, parrots, llamas, iron golems, snow golems, hoglins, zoglins, and boats.",
	"Some entities like the Ender Dragon, Wither, and Bats cannot be leashed."
})
@Example("""
	on right click:
		leash event-entity to player
		send "&aYou leashed &2%event-entity%!" to player
	""")
@Since("2.3, 2.10 (entities)")
public class EffLeash extends Effect {

	static {
		Skript.registerEffect(EffLeash.class,
			"(leash|lead) %entities% to %entity%",
			"make %entity% (leash|lead) %entities%",
			"un(leash|lead) [holder of] %entities%");
	}

	private Expression<Entity> holder;
	private Expression<Entity> targets;
	private boolean leash;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		leash = matchedPattern != 2;
		if (leash) {
			holder = (Expression<Entity>) exprs[1 - matchedPattern];
			targets = (Expression<Entity>) exprs[matchedPattern];
		} else {
			targets = (Expression<Entity>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Entity holder = leash ? this.holder.getSingle(event) : null;
		if (leash && holder == null)
			return;
		
		for (Entity target : targets.getArray(event)) {
			if (target instanceof Leashable leashable) {
				leashable.setLeashHolder(holder);
			}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (leash)
			return "leash " + targets.toString(e, debug) + " to " + holder.toString(e, debug);
		else
			return "unleash " + targets.toString(e, debug);
	}

}
