package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.loot.LootContext;

@Name("Looted Entity of Loot Context")
@Description("Returns the looted entity of a loot context.")
@Examples("looted entity of {_context}")
@Since("INSERT VERSION")
public class ExprLootContextEntity extends SimplePropertyExpression<LootContext, Entity> {

	static {
		register(ExprLootContextEntity.class, Entity.class, "[looted] entity", "lootcontexts");
	}

	@Override
	public Entity convert(LootContext context) {
		return context.getLootedEntity();
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "looted entity";
	}
}
