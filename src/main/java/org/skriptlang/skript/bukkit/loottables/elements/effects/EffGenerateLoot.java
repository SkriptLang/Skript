package org.skriptlang.skript.bukkit.loottables.elements.effects;

import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Name("Generate Loot")
@Description({
	"Generates the loot in the specified inventories from a loot table using a loot context. "
		+ "Not specifying a loot context will use a loot context with a location at the world's origin.",
	"Note that if the inventory is full, it will cause warnings in the console due to over-filling the inventory."
})
@Examples({
	"generate loot of loot table \"minecraft:chests/simple_dungeon\" using loot context at player in {_inventory}",
	"generate loot using \"minecraft:chests/shipwreck_supply\" in {_inventory}"
})
@Since("2.10")
public class EffGenerateLoot extends Effect implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffGenerateLoot.class)
			.addPattern("generate [the] loot (of|using) %loottable% [(with|using) %-lootcontext%] in %inventories%")
			.build()
		);
	}

	private Node node;
	private Expression<LootTable> lootTable;
	private Expression<LootContext> context;
	private Expression<Inventory> inventories;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		lootTable = (Expression<LootTable>) exprs[0];
		context = (Expression<LootContext>) exprs[1];
		inventories = (Expression<Inventory>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Random random = ThreadLocalRandom.current();

		LootContext context;
		if (this.context != null) {
			context = this.context.getSingle(event);
			if (context == null) {
				error("The provided loot context was not set.", this.context.toString());
				return;
			}
		} else {
			context = new LootContextWrapper(Bukkit.getWorlds().get(0).getSpawnLocation()).getContext();
		}

		LootTable table = lootTable.getSingle(event);
		if (table == null) {
			error("The provided loot table was not set.", this.lootTable.toString());
			return;
		}

		for (Inventory inventory : inventories.getArray(event)) {
			try {
				table.fillInventory(inventory, random, context);
			} catch (IllegalArgumentException ignored) {
				error("Failed to fill an inventory with the provided loot table (and loot context).", inventories.toString());
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("generate loot using", lootTable);
		if (context != null)
			builder.append("with", context);
		builder.append("in", inventories);

		return builder.toString();
	}

}
