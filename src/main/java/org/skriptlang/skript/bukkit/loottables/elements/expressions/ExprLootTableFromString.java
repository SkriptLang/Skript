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

@Name("Loot Table from Key")
@Description("Returns the loot table from a namespaced key.")
@Examples("set {_table} to loot table \"minecraft:chests/simple_dungeon\"")
@Since("INSERT VERSION")
public class ExprLootTableFromString extends SimpleExpression<LootTable> {

	static {
		Skript.registerExpression(ExprLootTableFromString.class, LootTable.class, ExpressionType.PROPERTY,
			"[the] loot[ ]table[s] %strings%",
			"%strings%'[s] loot[ ]table[s]"
		);
	}

	private Expression<String> key;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		//noinspection unchecked
		key = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected LootTable @Nullable [] get(Event event) {
		String key = this.key.getSingle(event);
		if (key == null)
			return new LootTable[0];

		NamespacedKey namespacedKey = NamespacedKey.fromString(key);
		if (namespacedKey == null)
			return new LootTable[0];

		return new LootTable[]{Bukkit.getLootTable(namespacedKey)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "loot table of "  + key.toString(event, debug);
	}

}
