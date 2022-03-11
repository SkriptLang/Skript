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
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Bed")
@Description("The bed location of a player, " +
	"i.e. the spawn point of a player if they ever slept in a bed and the bed still exists and is unobstructed.")
@Examples({
	"if bed of player exists:",
		"\tteleport player the the player's bed",
	"else:",
		"\tteleport the player to the world's spawn point"
})
@Since("2.0, INSERT VERSION (offlineplayers)")
public class ExprBed extends SimplePropertyExpression<OfflinePlayer, Location> {

	static {
		register(ExprBed.class, Location.class, "bed[s] [location[s]]", "offlineplayers");
	}

	@Override
	@Nullable
	public Location convert(OfflinePlayer p) {
		return p.getBedSpawnLocation();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			if (!Player.class.isAssignableFrom(getExpr().getReturnType())) {
				Skript.error("Bed location of offline players cannot be set/deleted.");
				return null;
			}
			return new Class[] {Location.class};
		}
		return null;
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		Location loc = delta == null ? null : (Location) delta[0];
		for (OfflinePlayer p : getExpr().getArray(e)) {
			if (p.isOnline()) // double check
				((Player) p).setBedSpawnLocation(loc, true);
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
