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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Projectile;

@Name("Projectile Can Bounce")
@Description("Whether or not a projectile can bounce.")
@Examples({"on shoot:",
	"\tsend \"Boing!\" to all players if projectile can bounce"})
@Since("2.5.1")
public class CondProjectileCanBounce extends PropertyCondition<Projectile> {

	static {
		register(CondProjectileCanBounce.class, PropertyType.CAN, "bounce", "projectiles");
	}

	@Override
	public boolean check(Projectile projectile) {
		return projectile.doesBounce();
	}

	@Override
	public String getPropertyName() {
		return "bounce";
	}

}
