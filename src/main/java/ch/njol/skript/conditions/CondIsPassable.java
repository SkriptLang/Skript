/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.block.Block;

@Name("Is Passable")
@Description({"Checks whether a block is passable.",
	"A block is passable if it has no colliding parts that would prevent players from moving through it.",
	"Blocks like tall grass, flowers, signs, etc. are passable, but open doors, fence gates, trap doors, etc. are not because they still have parts that can be collided with."
})
@Examples("if player's targeted block is passable")
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.13.2+")
public class CondIsPassable extends PropertyCondition<Block> {

	static {
		if (Skript.methodExists(Block.class, "isPassable")) {
			register(CondIsPassable.class, "passable", "blocks");
		}
	}

	@Override
	public boolean check(Block block) {
		return block.isPassable();
	}

	@Override
	protected String getPropertyName() {
		return "passable";
	}

}
