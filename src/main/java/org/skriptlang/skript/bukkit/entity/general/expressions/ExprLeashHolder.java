package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Leash Holder")
@Description("The leash holder of a living entity.")
@Example("set {_example} to the leash holder of the target mob")
@Since("2.3")
public class ExprLeashHolder extends SimplePropertyExpression<LivingEntity, Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprLeashHolder.class, Entity.class, "leash holder[s]", "livingentities", false)
				.supplier(ExprLeashHolder::new)
				.build()
		);
	}

	@Override
	public @Nullable Entity convert(LivingEntity entity) {
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
