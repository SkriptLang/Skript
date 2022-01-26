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
package ch.njol.skript.bukkitutil;

import ch.njol.util.Math2;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Damageable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class HealthUtils {

	/**
	 * Get the health of an entity
	 * @param e Entity to get health from
	 * @return The amount of hearts the entity has left
	 */
	public static double getHealth(Damageable e) {
		if (e.isDead())
			return 0;
		return e.getHealth() / 2;
	}
	
	/**
	 * Set the health of an entity
	 * @param e Entity to set health for
	 * @param health The amount of hearts to set
	 */
	public static void setHealth(Damageable e, double health) {
		e.setHealth(Math2.fit(0, health, getMaxHealth(e)) * 2);
	}
	
	/**
	 * Get the max health an entity has
	 * @param e Entity to get max health from
	 * @return How many hearts the entity can have at most
	 */
	public static double getMaxHealth(Damageable e) {
		AttributeInstance attributeInstance = ((Attributable) e).getAttribute(Attribute.GENERIC_MAX_HEALTH);
		assert attributeInstance != null;
		return attributeInstance.getValue() / 2;
	}
	
	/**
	 * Set the max health an entity can have
	 * @param e Entity to set max health for
	 * @param health How many hearts the entity can have at most
	 */
	public static void setMaxHealth(Damageable e, double health) {
		AttributeInstance attributeInstance = ((Attributable) e).getAttribute(Attribute.GENERIC_MAX_HEALTH);
		assert attributeInstance != null;
		attributeInstance.setBaseValue(health * 2);
	}
	
	/**
	 * Apply damage to an entity
	 * @param e Entity to apply damage to
	 * @param d Amount of hearts to damage
	 */
	public static void damage(Damageable e, double d) {
		if (d < 0) {
			heal(e, -d);
			return;
		}
		e.damage(d * 2);
	}

	/**
	 * Heal an entity
	 * @param e Entity to heal
	 * @param h Amount of hearts to heal
	 */
	public static void heal(Damageable e, double h) {
		if (h < 0) {
			damage(e, -h);
			return;
		}
		setHealth(e, getHealth(e) + h);
	}
	
	public static double getDamage(EntityDamageEvent e) {
		return e.getDamage() / 2;
	}
	
	public static double getFinalDamage(EntityDamageEvent e) {
		return e.getFinalDamage() / 2;
	}
	
	public static void setDamage(EntityDamageEvent e, double damage) {
		e.setDamage(damage * 2);
	}
	
	public static void setDamageCause(Damageable e, DamageCause cause) {
		e.setLastDamageCause(new EntityDamageEvent(e, cause, 0));
	}
	
}
