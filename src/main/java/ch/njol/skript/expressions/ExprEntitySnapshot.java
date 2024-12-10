package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
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
	"Returns the entity snapshot of a provided entity, which includes all the data associated with it "
	+ "(name, health, attributes, etc.) at the time it's retrieved.",
	"This acts as a template, and cannot be used to modify an entity's data. Individual attributes, like it's health for example, "
	+ "also cannot be retrieved or modified from a snapshot.",
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
public class ExprEntitySnapshot extends SimplePropertyExpression<Object, EntitySnapshot> {

	static {
		if (Skript.classExists("org.bukkit.entity.EntitySnapshot"))
			register(ExprEntitySnapshot.class, EntitySnapshot.class, "entity snapshot", "entities/entitydatas");
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
	public @Nullable EntitySnapshot convert(Object object) {
		if (object instanceof Entity entity) {
			return entity.createSnapshot();
		} else if (object instanceof EntityData<?> entityData) {
			Entity entity = entityData.create();
			EntitySnapshot snapshot = entity.createSnapshot();
			entity.remove();
			return snapshot;
		}
		return null;
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
