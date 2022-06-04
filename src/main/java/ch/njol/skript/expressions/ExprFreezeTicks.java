package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.eclipse.jdt.annotation.Nullable;

public class ExprFreezeTicks extends SimplePropertyExpression<Entity, Number> {
	@Override
	protected String getPropertyName() {
		return null;
	}

	@Override
	public @Nullable Number convert(Entity entity) {
		return entity.getFreezeTicks();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
}
