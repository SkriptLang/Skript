package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;

@Name("Has Loot Table")
@Description("Checks whether an entity or block has a loot table. The loot tables of chests will be deleted when the chest is opened or broken.")
@Examples("if block has a loot table:")
@Since("INSERT VERSION")
public class CondHasLootTable extends Condition {

	static {
		Skript.registerCondition(CondHasLootTable.class,
			"%entities/blocks% (has|have) [a] loot[ ]table",
			"%entities/blocks% does(n't| not) have [a] loot[ ]table",
			"%entities/blocks% (has|have) no loot[ ]table"
		);}

	private Expression<?> lootables;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		lootables = exprs[0];
		setNegated(matchedPattern > 0);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return lootables.check(event, (lootable) -> {
			if (LootTableUtils.isLootable(lootable))
				return LootTableUtils.getLootable(lootable).hasLootTable();
			return false;
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return lootables.toString(event, debug) + " has" + (isNegated() ? " no" : "") + " loot table";
	}

}

