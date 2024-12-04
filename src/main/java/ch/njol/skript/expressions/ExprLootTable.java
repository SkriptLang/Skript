package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;

@Name("Loot Table")
@Description({"Returns the loot table of an entity, block or loot table type.",
"Setting the loot table of a block will update the block state, and once opened will generate loot of the specified loot table. Please note that doing so may cause warnings in the console due to over-filling the chest."})
@Examples({
	"set {_loot} to loot table of event-entity",
	"set {_loot} to loot table of event-block",
	"",
	"set loot table of event-entity to \"entities/ghast\"",
	"# this will set the loot table of the entity to a ghast's loot table, thus dropping ghast tears and gunpowder",
	"",
	"set loot table of event-block to loot table of minecraft:chests/simple_dungeon"
})
@Since("INSERT VERSION")
public class ExprLootTable extends SimplePropertyExpression<Object, LootTable> {

	static {
		register(ExprLootTable.class, LootTable.class, "loot[ ]table[s]", "entities/blocks/loottabletype");
	}

	@Override
	public @Nullable LootTable convert(Object object) {
		if (object instanceof LootTables lootTables)
			return lootTables.getLootTable();
		if (object instanceof Lootable lootable)
			return lootable.getLootTable();
		if (object instanceof Block block)
			return block.getState() instanceof Lootable lootable ? lootable.getLootTable() : null;
		return null;
	}

	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(LootTable.class);
			default -> null;
        };
    }

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block)
				object = block.getState();
			if (!(object instanceof Lootable lootable))
				return;

			LootTable lootTable = delta != null ? ((LootTable) delta[0]) : null;
			if (mode == ChangeMode.SET && lootTable != null)
				lootable.setLootTable(lootTable);
			else if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
				lootable.clearLootTable();

			if (lootable instanceof BlockState blockState)
				blockState.update();
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
