package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Leash Holder")
@Description("The leash holder of a living entity.")
@Examples("set {_example} to the leash holder of the target mob")
@Since("2.3")
public class ExprLeashHolder extends SimplePropertyExpression<LivingEntity, Entity> {

	static {
		register(ExprLeashHolder.class, Entity.class, "leash holder[s]", "livingentities");
	}

	@Override
	@Nullable
	public Entity convert(LivingEntity entity) {
		return entity.isLeashed() ? entity.getLeashHolder() : null;
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
