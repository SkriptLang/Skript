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
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Allay Duplication Decree")
@Description({
	"Doth decree whether an allay may or may not duplicate itself.",
	"This is not the same as the breeding of allays."
})
@Example("grant all allays leave to duplicate")
@Example("forbid all allays from duplicating")
@Since("2.11")
public class EffAllayCanDuplicate extends Effect {

	static {
		Skript.registerEffect(EffAllayCanDuplicate.class,
			"grant %livingentities% leave to (duplicate|clone)",
			"forbid %livingentities% from (duplicating|cloning)");
	}

	private Expression<LivingEntity> entities;
	private boolean duplicate;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		duplicate = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Allay allay)
				allay.setCanDuplicate(duplicate);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (duplicate)
			return "allow " + entities.toString(event, debug) + " to duplicate";
		return "prevent " + entities.toString(event, debug) + " from duplicating";
	}

}
