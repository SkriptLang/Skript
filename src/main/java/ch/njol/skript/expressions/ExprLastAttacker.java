package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Last Attacker")
@Description("The last block or entity that attacked an entity.")
@Example("send \"%last attacker of event-entity%\"")
@Since("2.5.1")
public class ExprLastAttacker extends SimplePropertyExpression<Entity, Entity> {

	static {
		register(ExprLastAttacker.class, Entity.class, "last attacker", "entity");
	}

	@Override
	public @Nullable Entity convert(Entity entity) {
		return ExprAttacker.getAttacker(entity.getLastDamageCause());
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "last attacker";
	}

}
