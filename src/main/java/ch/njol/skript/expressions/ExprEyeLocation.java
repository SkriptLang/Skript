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
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Head location")
@Description({"The location of an entity's head, mostly useful for players and e.g. looping blocks in the player's line of sight.",
		"Please note that this location is only accurate for entities whose head is exactly above their center, i.e. players, endermen, zombies, skeletons, etc., but not sheep, pigs or cows."})
@Examples({"set the block at the player's head to air",
		"set the block in front of the player's eyes to glass",
		"loop blocks in front of the player's head:"})
@Since("2.0")
public class ExprEyeLocation extends SimplePropertyExpression<LivingEntity, Location> {
	static {
		register(ExprEyeLocation.class, Location.class, "(head|eye[s]) [location[s]]", "livingentities");
	}
	
	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "eye location";
	}
	
	@Override
	@Nullable
	public Location convert(final LivingEntity e) {
		return e.getEyeLocation();
	}
	
}
