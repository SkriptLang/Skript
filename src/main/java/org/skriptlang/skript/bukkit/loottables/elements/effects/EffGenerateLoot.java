package org.skriptlang.skript.bukkit.loottables.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Name("Generate Loot")
@Description({
	"Generates the loot in the specified inventories from a loot table using a loot context. " +
		"Some loot tables will require some of these values whereas others may not.",
	"Note that if the inventory is full, it will cause warnings in the console due to over-filling the inventory.",
})
@Examples("generate loot of loot table \"minecraft:chests/simple_dungeon\" using loot context at player in {_inventory}")
@Since("INSERT VERSION")
public class EffGenerateLoot extends Effect {

	static {
		Skript.registerEffect(EffGenerateLoot.class,
			"generate loot (of|using) [[the] loot[ ]table] %loottable% (with|using) [[the] [loot] context] %lootcontext% in [inventor(y|ies)] %inventories%"
		);
	}

	private Expression<LootTable> lootTable;
	private Expression<LootContext> lootContext;
	private Expression<Inventory> inventories;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lootTable = (Expression<LootTable>) exprs[0];
		lootContext = (Expression<LootContext>) exprs[1];
		inventories = (Expression<Inventory>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Random random = ThreadLocalRandom.current();

		LootContext context = lootContext.getSingle(event);
		LootTable table = lootTable.getSingle(event);
		if (context == null || table == null)
			return;

		for (Inventory inventory : inventories.getArray(event)) {
			try {
				table.fillInventory(inventory, random, context);
			} catch (IllegalArgumentException ignore) {}
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