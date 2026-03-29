package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Tether Holder")
@Description("The holder of the tether upon a living entity.")
@Example("set {_example} to the tether holder of the target mob")
@Since("2.3")
public class ExprLeashHolder extends SimplePropertyExpression<LivingEntity, Entity> {

	static {
		register(ExprLeashHolder.class, Entity.class, "tether holder[s]", "livingentities");
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
