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
import ch.njol.skript.lang.ExpressionType;
import edu.umd.cs.findbugs.ba.npe.LocationWhereValueBecomesNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

@Name("Last Death Location")
@Description("The location of a player's last death.")
@Examples("set {lastdeathlocation::%player's uuid%} to last death location of player")
@Since("INSERT VERSION")
public class ExprLastDeathLocation extends SimplePropertyExpression<Player, Location> {
	
	static {
		register(ExprLastDeathLocation.class, Location.class, "last death location", "players");
	}

	@Override
	@Nullable
	public Location convert(Player player) {
		return player.getLastDeathLocation();
	}
	
	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "last death location";
	}
	
}
