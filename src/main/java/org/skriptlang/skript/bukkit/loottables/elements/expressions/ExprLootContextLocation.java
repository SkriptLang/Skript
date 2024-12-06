package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.Nullable;

@Name("Loot Location of Loot Context")
@Description("Returns the loot context location of a loot context.")
@Examples("set {_location} to loot context location of {_context}")
@Since("INSERT VERSION")
public class ExprLootContextLocation extends SimplePropertyExpression<LootContext, Location> {

	static {
		registerDefault(ExprLootContextLocation.class, Location.class, "loot [context] location", "lootcontexts");
	}

	@Override
	public @Nullable Location convert(LootContext context) {
		return context.getLocation();
	}

	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}

	@Override
	protected String getPropertyName() {
		return "location of loot context";
	}

}
