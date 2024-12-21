package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Loot Table from Key")
@Description("Returns the loot table from a namespaced key.")
@Examples("set {_table} to loot table \"minecraft:chests/simple_dungeon\"")
@Since("INSERT VERSION")
public class ExprLootTableFromString extends SimpleExpression<LootTable> {

	static {
		Skript.registerExpression(ExprLootTableFromString.class, LootTable.class, ExpressionType.COMBINED,
			"[the] loot[ ]table[s] %strings%"
		);
	}

	private Expression<String> keys;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		//noinspection unchecked
		keys = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected LootTable @Nullable [] get(Event event) {
		List<LootTable> lootTables = new ArrayList<>();
		for (String key : keys.getArray(event)) {
			NamespacedKey namespacedKey = NamespacedKey.fromString(key);
			if (namespacedKey == null)
				continue;

			LootTable lootTable = Bukkit.getLootTable(namespacedKey);
			if (lootTable != null)
				lootTables.add(lootTable);
		}

		return lootTables.toArray(new LootTable[0]);
	}

	@Override
	public boolean isSingle() {
		return keys.isSingle();
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the loot table of " + keys.toString(event, debug);
	}

}
