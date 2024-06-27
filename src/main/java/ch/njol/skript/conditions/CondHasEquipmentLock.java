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

/*

[un]lock %equipmentslots% of %armorstands% [with %lockingmechanism%]

equipment lock[s] of %armorstands%
^ should this support set/add/remove? should there be an expression to create an equipment lock object?
^
^ set {_x} to equipment lock for %equipment slot% with locking mechanism %lockingmechanism%
^ add {_x} to equipment locks of %armorstands%

if %armor stands% has %equipmentlocks% [(for|with) %locking type%]

if %itemtypes% (are able to|can) be (added|added or (swapped|changed)|removed or (swapped|changed)) (to|in|with) [the] %equipmentslots% of %livingentities%

 */

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class CondHasEquipmentLock extends Condition {

	static {
		Skript.registerCondition(CondHasEquipmentLock.class,
			"[an] item[s] can be (0:added|1:added or (swapped|changed)|2:removed or (swapped|changed)) to [the] %equipmentslots% of %livingentities%",
			"[an] item[s] (can't|cannot|can not) be (added|added or (swapped|changed)|removed or (swapped|changed)) to [the] %equipmentslots% of %livingentities%"
		);
	}

	private static final int ADDING = 0;
	private static final int ADDING_OR_CHANGING = 1;
	private static final int REMOVING_OR_CHANGING = 2;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<EquipmentSlot> slots;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;
	private int lockType;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		slots = (Expression<EquipmentSlot>) exprs[1];
		entities = (Expression<Entity>) exprs[0];
		lockType = parseResult.mark;
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event,
			entity -> {
				return slots.check(event,
					slot -> {
						return true;
					});
			});
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "items can " + (isNegated() ? "not " : "") + "be added to " + slots.toString(event, debug) + " of " + entities.toString(event, debug);
	}

}
