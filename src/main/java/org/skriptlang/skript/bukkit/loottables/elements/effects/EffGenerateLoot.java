package org.skriptlang.skript.bukkit.loottables.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Name("Generate Loot")
@Description({
	"Generates the loot in the specified inventories from a loot table using a loot context.",
	"Note that loot contexts require the killer and looted entity if the loot table is under the entities category. It only requires the location if the loot table is not in the entities category, eg. blocks, chests.",
	"Also note that if the inventory is full, it will cause warnings in the console due to over-filling the inventory."
})
@Examples("the loot context at {_location}")
@Since("INSERT VERSION")
public class EffGenerateLoot extends Effect {

	static {
		Skript.registerEffect(EffGenerateLoot.class,
			"generate loot (of|using) [loot[ ]table] %loottable% (with|using) [[loot] context] %lootcontext% in [inventor(y|ies)] %inventories%"
		);
	}

	private Expression<LootTable> lootTable;
	private Expression<LootContextWrapper> lootContext;
	private Expression<Inventory> inventories;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		lootTable = (Expression<LootTable>) exprs[0];
		lootContext = (Expression<LootContextWrapper>) exprs[1];
		inventories = (Expression<Inventory>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Random random = ThreadLocalRandom.current();

		LootContextWrapper wrapper = lootContext.getSingle(event);
		LootTable table = lootTable.getSingle(event);
		if (wrapper == null || table == null)
			return;

		LootContext context = wrapper.getContext();
		if (context == null)
			return;

		for (Inventory inventory : inventories.getArray(event)) {
			try {
				table.fillInventory(inventory, random, context);
			}
			catch (IllegalArgumentException ignore) {}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("generate loot using loot table", lootTable);
		builder.append("with context", lootContext);
		builder.append("in inventories", inventories);

		return builder.toString();
	}

}
