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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fuel Level")
@Description("Get the level of current fuel of a brewing stand. can be modified")
@Examples({
	"on fuel brewing:",
		"\tset fuel level of target block to 100"
})
@Since("INSERT VERSION")
public class ExprFuelLevel extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprFuelLevel.class, Integer.class, ExpressionType.PROPERTY, "[the] fuel level of %block%");
	}

	private static final ItemType brewing_stand = Aliases.javaItemType("brewing stand");

	private Expression<Block> block;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		block = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Integer[] get(Event event) {
		Block block = this.block.getSingle(event);
		if (block == null || !brewing_stand.isOfType(block))
			return new Integer[0];
		return new Integer[] {((BrewingStand) block.getState()).getFuelLevel()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	@Nullable
	public String toString(Event event, boolean debug) {
		return "the fuel power of " + block.toString(event, debug);
	}
}
