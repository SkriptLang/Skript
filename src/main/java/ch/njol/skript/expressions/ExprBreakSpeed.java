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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Block Break Speed")
@Description(
	"Gets the speed at which the given player would break this block, taking into account tools, potion effects, " +
	"whether or not the player is in water, enchantments, etc. The returned value is the amount of progress made in " +
	"breaking the block each tick. When the total breaking progress reaches 1.0, the block is broken. Note that the " +
	"break speed can change in the course of breaking a block, e.g. if a potion effect is applied or expires, or the " +
	"player jumps/enters water.")
@Examples({
	"on left click using diamond pickaxe:",
		"\tevent-block is set",
		"\tsend \"Break Speed: %break speed for player%\" to player"
})
@Since("INSERT VERSION")
@RequiredPlugins("1.17+")
public class ExprBreakSpeed extends SimpleExpression<Number> {

	static {
		if (Skript.methodExists(Block.class, "getBreakSpeed", Player.class)) {
			Skript.registerExpression(ExprBreakSpeed.class, Number.class, ExpressionType.COMBINED,
				"[the] break speed [of %block%] [for %player%]",
				"%block%'[s] break speed [for %player%]"
			);
		}
	}

	private Expression<Block> block;
	private Expression<Player> player;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		block = (Expression<Block>) exprs[0];
		player = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	protected @Nullable Number[] get(Event event) {
		Block block = this.block.getSingle(event);
		if (block == null)
			return new Number[0];

		Player player = this.player.getSingle(event);
		if (player == null)
			return new Number[0];

		return new Number[]{block.getBreakSpeed(player)};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "break speed of " + block.toString(event, debug) + " for " + player.toString(event, debug);
	}
}
