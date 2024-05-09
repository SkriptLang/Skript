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
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

@Name("Bell Is Ringing")
@Description("Checks to see if a bell is currently ringing. A bell typically rings for 50 game ticks.")
@Examples("target block is ringing")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class CondIsRinging extends PropertyCondition<Block> {

	static {
		if (Skript.classExists("org.bukkit.block.Bell") && Skript.methodExists(Bell.class, "isShaking"))
			register(CondIsRinging.class, "ringing", "blocks");
	}

	@Override
	public boolean check(Block value) {
		BlockState state = value.getState(false);
		return state instanceof Bell && ((Bell) state).isShaking();
	}

	@Override
	protected String getPropertyName() {
		return "ringing";
	}

}
