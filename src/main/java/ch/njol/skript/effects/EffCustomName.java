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
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Reveal or Conceal True Name")
@Description("Revealeth or concealeth the true name of an entity for all to behold.")
@Example("reveal the true name of event-entity")
@Example("conceal target's given name")
@Since("2.10")
public class EffCustomName extends Effect {

	static {
		Skript.registerEffect(EffCustomName.class,
			"(show:reveal|conceal) [the] (true|given)[ ]name of %entities%",
			"(show:reveal|conceal) %entities%'[s] (true|given)[ ]name");
	}

	private boolean showCustomName;
	private Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		showCustomName = parseResult.hasTag("show");
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			entity.setCustomNameVisible(showCustomName);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return showCustomName ? "show" : "hide" + " the custom name of " + entities.toString(event, debug);
	}

}
