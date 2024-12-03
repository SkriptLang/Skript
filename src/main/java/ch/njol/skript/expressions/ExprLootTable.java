/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
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
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;

@Name("Loot Table")
@Description("Returns the loot table of an entity or block.")
@Examples({
	"set {_loot} to loot table of event-entity",
	"set {_loot} to loot table of event-block",
	"",
	"set loot table of event-entity to \"entities/ghast\"",
	"# this will set the loot table of the entity to a ghast's loot table, thus dropping ghast tears and gunpowder",
	"",
	"set loot table of event-block to loot table from \"minecraft:chests/simple_dungeon\""
})
@Since("INSERT VERSION")
public class ExprLootTable extends SimplePropertyExpression<Object, LootTable> {

	static {
		register(ExprLootTable.class, LootTable.class, "loot[ ]table[s]", "entities/blocks");
	}

	@Override
	public @Nullable LootTable convert(Object object) {
		if (object instanceof Lootable lootable)
			return lootable.getLootTable();
		if (object instanceof Block block)
			return block.getState() instanceof Lootable lootable ? lootable.getLootTable() : null;
		return null;
	}

	public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(LootTable.class);
			default -> null;
        };
    }

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block)
				object = block.getState();
			if (!(object instanceof Lootable lootable))
				return;

			LootTable lootTable = delta != null ? ((LootTable) delta[0]) : null;
			if (mode == Changer.ChangeMode.SET && lootTable != null)
				lootable.setLootTable(lootTable);
			else if (mode == Changer.ChangeMode.DELETE)
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
