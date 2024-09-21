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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffEquipmentLock extends Effect {

	static {
		if (Skript.classExists("org.bukkit.entity.ArmorStand$LockType"))
			Skript.registerEffect(EffEquipmentLock.class,
				"(:allow|prevent) items (to be|from being) (0:added|1:added and changed|2:removed and changed) (to|with|in) [the] %equipmentslots% of %livingentities%",
				"(:allow|prevent) items (to be|from being) (0:added|1:added and changed|2:removed and changed) (to|with|in) [the] %livingentities%'[s] %equipmentslots%"
			);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<EquipmentSlot> slots;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private ArmorStand.LockType lockType;
	private boolean allow;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		slots = matchedPattern == 0 ? (Expression<EquipmentSlot>) exprs[0] : (Expression<EquipmentSlot>) exprs[1];
		entities = matchedPattern == 0 ? (Expression<Entity>) exprs[1] : (Expression<Entity>) exprs[0];
		allow = parseResult.hasTag("allow");
		if (parseResult.mark == 0) {
			lockType = ArmorStand.LockType.ADDING;
		} else if (parseResult.mark == 1) {
			lockType = ArmorStand.LockType.ADDING_OR_CHANGING;
		} else if (parseResult.mark == 2) {
			lockType = ArmorStand.LockType.REMOVING_OR_CHANGING;
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand) {
				for (EquipmentSlot slot : slots.getArray(event)) {
					boolean alreadyHas = ((ArmorStand) entity).hasEquipmentLock(slot.getEquipSlot().asBukkitSlot(), lockType);
					if (!allow && alreadyHas) {
						((ArmorStand) entity).addEquipmentLock(slot.getEquipSlot().asBukkitSlot(), lockType);
					} else if (allow && !alreadyHas) {
						((ArmorStand) entity).removeEquipmentLock(slot.getEquipSlot().asBukkitSlot(), lockType);
					}
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String type;
		if (lockType == ArmorStand.LockType.ADDING) {
			type = "added";
		} else if (lockType == ArmorStand.LockType.ADDING_OR_CHANGING) {
			type = "added or changed";
		} else {
			type = "removed or changed";
		}
		return (allow ? "allow " : "prevent ") + "items to be " + type + " to the " + slots.toString(event, debug) + " of " + entities.toString(event, debug);
	}
}
