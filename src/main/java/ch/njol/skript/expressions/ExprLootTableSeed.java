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
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;

@Name("Loot Table Seed")
@Description("Returns the seed of a loot table.")
@Examples({
	"seed of loot table of block",
	"set seed of loot table of block to 123456789"
})
@Since("INSERT VERSION")
public class ExprLootTableSeed extends SimplePropertyExpression<Object, Long> {

	static {
		register(ExprLootTableSeed.class, Long.class, "loot[ ]table seed[s]", "entities/blocks");
	}

	@Override
	public @Nullable Long convert(Object object) {
		if (object instanceof Lootable lootable)
			return lootable.getSeed();
		if (object instanceof Block block)
			return block.getState() instanceof Lootable lootable ? lootable.getSeed() : null;
		return null;
	}

	public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block)
				object = block.getState();
			if (!(object instanceof Lootable lootable))
				return;

			Number seed = delta != null ? ((Number) delta[0]) : null;
			if (seed == null)
				return;

			lootable.setSeed(seed.longValue());

			if (lootable instanceof BlockState blockState)
				blockState.update();
		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "loot table seed";
	}
}
