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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExprAttachedBlock extends SimpleExpression<Block> {

	private static final boolean HAS_ABSTRACT_ARROW = Skript.classExists("org.bukkit.entity.AbstractArrow");

	static {
		Skript.registerExpression(ExprAttachedBlock.class, Block.class, ExpressionType.COMBINED,
			"%projectiles%'s attached block",
			"attached block (at|of) %projectiles%"
		);
	}

	private Expression<Projectile> projectiles;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		projectiles = ((Expression<Projectile>) exprs[0]);
		return true;
	}

	@Override
	@Nullable
	protected Block[] get(Event event) {
		List<Block> blocks = new ArrayList<>();
		for (Projectile projectile : projectiles.getArray(event)) {
			if (HAS_ABSTRACT_ARROW) {
				if (projectile instanceof AbstractArrow) {
					blocks.add(((AbstractArrow) projectile).getAttachedBlock());
				}
				continue;
			}
			if (projectile instanceof Arrow) {
				blocks.add(((Arrow) projectile).getAttachedBlock());
			}
		}
		if (blocks.isEmpty())
			return new Block[0];
		return blocks.toArray(new Block[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "attached block at " + projectiles.toString(event, debug);
	}

}
