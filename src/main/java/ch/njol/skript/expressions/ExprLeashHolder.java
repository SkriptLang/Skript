package ch.njol.skript.expressions;

import io.papermc.paper.entity.Leashable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Leash Holder")
@Description("The leash holder of a leashable entity.")
@Examples("set {_example} to the leash holder of the target entity")
@Since("2.3")
public class ExprLeashHolder extends SimplePropertyExpression<Entity, Entity> {

	static {
		register(ExprLeashHolder.class, Entity.class, "leash holder[s]", "entities");
	}

	@Override
	@Nullable
	public Entity convert(Entity entity) {
		if (entity instanceof Leashable leashable && leashable.isLeashed()) {
			return leashable.getLeashHolder();
		}
		return null;
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "leash holder";
	}

}
