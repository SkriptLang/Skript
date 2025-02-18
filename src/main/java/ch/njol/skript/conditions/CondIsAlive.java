package ch.njol.skript.conditions;

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Is Alive")
@Description("Checks whether an entity is alive. Works for non-living entities too.")
@Examples({"if {villager-buddy::%player's uuid%} is not dead:",
	"",
	"on shoot:",
	"\twhile the projectile is alive:"})
@Since("2.0, 2.4-alpha4 (non-living entity support)")
public class CondIsAlive extends PropertyCondition<Entity> {

	static {
		register(CondIsAlive.class, "(alive|1¦dead)", "entities");
	}

	private boolean isNegated;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isNegated = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Entity e) {
		return isNegated == e.isDead();
	}

	@Override
	protected String getPropertyName() {
		return isNegated ? "dead" : "alive";
	}

}
