package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;

@Name("Luck of Loot Context")
@Description("Returns the number of luck of a loot context.")
@Examples("luck value of {_context}")
@Since("INSERT VERSION")
public class ExprLootContextLuck extends SimplePropertyExpression<LootContext, Float> {

	static {
		register(ExprLootContextLuck.class, Float.class, "luck [factor|value]", "lootcontexts");
	}

	@Nullable
	@Override
	public Float convert(LootContext context) {
		return context.getLuck();
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return "luck value";
	}
}
