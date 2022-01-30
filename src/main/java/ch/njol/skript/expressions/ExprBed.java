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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Bed")
@Description("The bed location of a player, i.e. the spawn point of a player if he ever slept in a bed and the bed still exists and is unobstructed.")
@Examples({"if bed of player exists:",
		"	teleport player the the player's bed",
		"else:",
		"	teleport the player to the world's spawn point"})
@Since("2.0")
public class ExprBed extends SimplePropertyExpression<Player, Location> {
	static {
		register(ExprBed.class, Location.class, "bed[s] [location[s]]", "players");
	}
	
	@Override
	@Nullable
	public Location convert(final Player p) {
		return p.getBedSpawnLocation();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return new Class[] {Location.class};
		return null;
	}
	
	@Override
	public void change(final Event e, @Nullable final Object[] delta, final ChangeMode mode) {
		if (delta == null) {
			for (final Player p : getExpr().getArray(e)) {
				p.setBedSpawnLocation(null, true);
			}
		} else {
			final Location l = (Location) delta[0];
			for (final Player p : getExpr().getArray(e)) {
				p.setBedSpawnLocation(l, true);
			}
		}
	}
	
	@Override
	protected String getPropertyName() {
		return "bed";
	}
	
	@Override
	public Class<? extends Location> getReturnType() {
		return Location.class;
	}
	
}
