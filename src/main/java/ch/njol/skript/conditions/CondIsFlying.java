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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Player;

/**
 * @author Peter Güttinger
 */
@Name("Is Flying")
@Description("Checks whether a player is flying.")
@Examples("player is not flying")
@Since("1.4.4")
public class CondIsFlying extends PropertyCondition<Player> {

	static {
		register(CondIsFlying.class, "flying", "players");
	}

	@Override
	public boolean check(final Player p) {
		return p.isFlying();
	}

	@Override
	protected String getPropertyName() {
		return "flying";
	}

}
