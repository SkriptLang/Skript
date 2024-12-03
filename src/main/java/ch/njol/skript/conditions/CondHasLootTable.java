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
package ch.njol.skript.conditions;

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
			if (lootable instanceof Lootable it) {
				return it.hasLootTable();
			} else if (lootable instanceof Block block) {
				return block.getState() instanceof Lootable it && it.hasLootTable();
			} else {
				return false;
			}
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return lootables.toString(event, debug) + " has" + (isNegated() ? " no" : "") + " loot table";
	}
}
