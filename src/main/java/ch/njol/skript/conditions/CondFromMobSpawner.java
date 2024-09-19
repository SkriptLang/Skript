package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;

@Name("Is From A Mob Spawner")
@Description("Check if an entity was spawned from a mob spawner.")
@Examples({
	"send whether target is from a mob spawner"
})
@Since("INSERT VERSION")
public class CondFromMobSpawner extends PropertyCondition<Entity> {

	static {
		if (Skript.methodExists(Entity.class, "fromMobSpawner"))
			Skript.registerCondition(CondFromMobSpawner.class,
				"%entities%('s | is| are) from [a] [mob] spawner",
				"%entities% (isn't|aren't|is not|are not) from [a] [mob] spawner");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		setExpr((Expression<Entity>) exprs[0]);
		return true;
	}

	@Override
	public boolean check(Entity entity) {
		return entity.fromMobSpawner();
	}

	@Override
	protected String getPropertyName() {
		return "from a mob spawner";
	}

}

