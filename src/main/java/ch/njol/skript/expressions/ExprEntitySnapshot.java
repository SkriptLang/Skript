package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Name("Entity Snapshot")
@Description({
	"Returns an entity snapshot of the provided entities, which is a snapshot of all the data of the provided entities.",
	"This data includes the values of all the entities' attributes at the time of this expression being called.",
	"This expression can only be used to copy the full attributes of an entity. Individual attributes cannot be modified or retrieved."
})
@Examples({
	"spawn a pig at location(0, 0, 0):",
		"\tset the max health of entity to 20",
		"\tset the health of entity to 20",
		"\tset {_snapshot} to the entity snapshot of entity",
		"\tclear entity",
	"spawn {_snapshot} at location(0, 0, 0)"
})
@RequiredPlugins("Minecraft 1.20.2+")
@Since("INSERT VERSION")
public class ExprEntitySnapshot extends SimplePropertyExpression<Entity, EntitySnapshot> {

	static {
		if (Skript.classExists("org.bukkit.entity.EntitySnapshot"))
			register(ExprEntitySnapshot.class, EntitySnapshot.class, "entity snapshot", "entities");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (Player.class.isAssignableFrom(exprs[0].getReturnType())) {
			Skript.error("You can't get a snapshot of a player.");
			return false;
		}
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable EntitySnapshot convert(Entity entity) {
		return entity.createSnapshot();
	}

	@Override
	protected String getPropertyName() {
		return "entity snapshot";
	}

	@Override
	public Class<EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

}
