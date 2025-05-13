package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.loot.LootContext;

@Name("Loot Context")
@Description("The loot context involved in the context create section.")
@Examples({
	"set {_context} to a new loot context at {_location}:",
		"\tbroadcast loot context"
})
@Since("INSERT VERSION")
public class ExprLootContext extends EventValueExpression<LootContext> {

	static {
		register(ExprLootContext.class, LootContext.class, "loot[ ]context");
	}

	public ExprLootContext() {
		super(LootContext.class);
	}

	@Override
	public String toString() {
		return "the loot context";
	}

}
