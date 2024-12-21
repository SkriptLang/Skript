package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Name("Loot of Loot Table")
@Description(
	"Returns the items of a loot table using a loot context. "
		+ "Not specifying a loot context will use a loot context with a location at the world's origin."
)
@Examples({
	"set {_items::*} to loot items of the loot table \"minecraft:chests/simple_dungeon\" with loot context {_context}",
	"# this will set {_items::*} to the items that would be dropped from the simple dungeon loot table with the given loot context",
	"",
	"give player loot items of entity's loot table with loot context {_context}",
	"# this will give the player the items that the entity would drop with the given loot context"
})
@Since("INSERT VERSION")
public class ExprLootItems extends SimpleExpression<ItemStack> {

	static {
		Skript.registerExpression(ExprLootItems.class, ItemStack.class, ExpressionType.COMBINED,
			"[the] loot of %loottables% [(with|using) %-lootcontext%]",
			"%loottables%'[s] loot [(with|using) %-lootcontext%]"
		);
	}

	private Expression<LootTable> lootTables;
	private Expression<LootContext> context;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lootTables = (Expression<LootTable>) exprs[0];
		context = (Expression<LootContext>) exprs[1];
		return true;
	}

	@Override
	protected ItemStack @Nullable [] get(Event event) {
		LootContext context;
		if (this.context != null) {
			context = this.context.getSingle(event);
			if (context == null)
				return new ItemStack[0];
		} else {
			context = new LootContextWrapper(Bukkit.getWorlds().get(0).getSpawnLocation()).getContext();
		}

		List<ItemStack> items = new ArrayList<>();

		Random random = ThreadLocalRandom.current();
		for (LootTable lootTable : lootTables.getArray(event)) {
			try {
				// todo: perhaps runtime error in the future
				items.addAll(lootTable.populateLoot(random, context));
			} catch (IllegalArgumentException ignore) {}
		}

		return items.toArray(new ItemStack[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);

		builder.append("the loot of", lootTables);
		if (context != null)
			builder.append("with", context);

		return builder.toString();
	}

}
