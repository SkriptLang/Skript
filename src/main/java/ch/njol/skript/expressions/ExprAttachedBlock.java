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
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.Nullable;

@Name("Arrow Attached Block")
@Description("Returns the attached block of an arrow.")
@Examples("set hit block of last shot arrow to diamond block")
@Since("2.8.0")
public class ExprAttachedBlock extends SimplePropertyExpression<Projectile, Block> {

	private static final boolean HAS_ABSTRACT_ARROW = Skript.classExists("org.bukkit.entity.AbstractArrow");

	static {
		register(ExprAttachedBlock.class, Block.class, "(attached|hit) block", "projectiles");
	}

	@Override
	@Nullable
	public Block convert(Projectile projectile) {
		if (HAS_ABSTRACT_ARROW) {
			if (projectile instanceof AbstractArrow) {
				return ((AbstractArrow) projectile).getAttachedBlock();
			}
		} else if (projectile instanceof Arrow) {
			return ((Arrow) projectile).getAttachedBlock();
		}
		return null;
	}

	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}

	@Override
	public String getPropertyName() {
		return "attached block";
	}

}
