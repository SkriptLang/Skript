package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import io.papermc.paper.entity.Leashable;

@Name("Leash Holder")
@Description("The leash holder of an entity.")
@Example("set {_example} to the leash holder of the target mob")
@Since("2.3, INSERT VERSION (non-living entities)")
public class ExprLeashHolder extends SimplePropertyExpression<Entity, Entity> {

	static {
		register(ExprLeashHolder.class, Entity.class, "leash holder[s]", "entities");
	}

	@Override
	public @Nullable Entity convert(Entity entity) {
		if (entity instanceof Leashable leashable) {
			return leashable.isLeashed() ? leashable.getLeashHolder() : null;
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
