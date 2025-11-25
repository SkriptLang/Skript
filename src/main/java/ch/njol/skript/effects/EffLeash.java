package ch.njol.skript.effects;

import io.papermc.paper.entity.Leashable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Leash entities")
@Description({
	"Leash entities to other entities. This works with all leashable entities including living entities and boats.",
	"See <a href=\"https://jd.papermc.io/paper/1.21.10/io/papermc/paper/entity/Leashable.html\">Paper's Javadocs for more info</a>."
})
@Examples({
	"on right click:",
		"\tleash event-entity to player",
		"\tsend \"&aYou leashed &2%event-entity%!\" to player"
})
@Since("2.3")
public class EffLeash extends Effect {

	static {
		Skript.registerEffect(EffLeash.class,
			"(leash|lead) %entities% to %entity%",
			"make %entity% (leash|lead) %entities%",
			"un(leash|lead) [holder of] %entities%");
	}

	@SuppressWarnings("null")
	private Expression<Entity> holder;
	@SuppressWarnings("null")
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
	protected void execute(Event e) {
		if (leash) {
			Entity holder = this.holder.getSingle(e);
			if (holder == null)
				return;
			for (Entity target : targets.getArray(e)) {
				if (target instanceof Leashable leashable)
					leashable.setLeashHolder(holder);
			}
		} else {
			for (Entity target : targets.getArray(e)) {
				if (target instanceof Leashable leashable)
					leashable.setLeashHolder(null);
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
