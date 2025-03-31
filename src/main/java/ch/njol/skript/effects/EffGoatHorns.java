package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Goat Horns")
@Description("Make a goat have or not have a left, right, or both horns.")
@Examples({
	"remove the left horn of last spawned goat",
	"regrow {_goat}'s horns",
	"remove both horns of all goats"
})
@Since("INSERT VERSION")
public class EffGoatHorns extends Effect {

	public enum GoatHorn {
		LEFT, RIGHT, BOTH, ANY
	}

	static {
		Skript.registerEffect(EffGoatHorns.class,
			"remove [the] (left horn[s]|right:right horn[s]|both:both horns) of %livingentities%",
			"remove %livingentities%'[s] (left horn[s]|right:right horn[s]|both:horns)",
			"(regrow|replace) [the] (left horn[s]|right:right horn[s]|both:both horns) of %livingentities%",
			"(regrow|replace) %livingentities%'[s] (left horn[s]|right:right horn[s]|both:horns)");
	}

	private Expression<LivingEntity> entities;
	private GoatHorn goatHorn = GoatHorn.LEFT;
	private boolean remove;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.hasTag("right")) {
			goatHorn = GoatHorn.RIGHT;
		} else if (parseResult.hasTag("both")) {
			goatHorn = GoatHorn.BOTH;
		}
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		remove = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Goat goat) {
				if (goatHorn != GoatHorn.RIGHT)
					goat.setLeftHorn(remove);
				if (goatHorn != GoatHorn.LEFT)
					goat.setRightHorn(remove);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (remove) {
			builder.append("remove");
		} else {
			builder.append("regrow");
		}
		builder.append(switch (goatHorn) {
			case LEFT -> "the left horn";
			case RIGHT -> "the right horn";
			case BOTH -> "both horns";
			case ANY -> "any horn";
		});
		builder.append("of", entities);
		return builder.toString();
	}

}
