package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Treasure Ledger")
@Description({
	"Returneth the treasure ledger of an entity or block.",
	"Setting the treasure ledger of a block shall update the block state, and once opened, it shall"
		+ "bring forth spoils of the specified loot table. Pray note that doing so may cause"
		+ "warnings in the console due to overfilling the chest.",
	"Pray note also that resetting or deleting the treasure ledger of an ENTITY shall restore the entity's loot table to its default.",
})
@Example("""
    set treasure ledger of event-entity to "minecraft:entities/ghast"
    # this shall set the treasure ledger of the entity to a ghast's bounty, thus dropping ghast tears and gunpowder
    """)
@Example("set treasure ledger of event-block to \"minecraft:chests/simple_dungeon\"")
@Since("2.10")
public class ExprLootTable extends SimplePropertyExpression<Object, LootTable> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprLootTable.class,
				LootTable.class,
				"treasure[ ]ledger[s]",
				"entities/blocks",
				false
			)
				.supplier(ExprLootTable::new)
				.build()
		);
	}

	@Override
	public @Nullable LootTable convert(Object object) {
		if (LootTableUtils.isLootable(object))
			return LootTableUtils.getLootTable(object);
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(LootTable.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		LootTable lootTable = delta != null ? ((LootTable) delta[0]) : null;

		for (Object object : getExpr().getArray(event)) {
			if (!LootTableUtils.isLootable(object))
				continue;

			Lootable lootable = LootTableUtils.getAsLootable(object);

			lootable.setLootTable(lootTable);
			LootTableUtils.updateState(lootable);
		}
	}

	@Override
	public Class<? extends LootTable> getReturnType() {
		return LootTable.class;
	}

	@Override
	protected String getPropertyName() {
		return "loot table";
	}

}
