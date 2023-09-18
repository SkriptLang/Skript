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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Can Pick Up Items")
@Description("Whether living entities are able to pick up items off the ground or not.")
@Examples({
	"if player can pick items up:",
		"\tsend \"You can pick up items!\" to player",
	"",
	"on drop:",
		"\tif player can't pick	up items:",
			"\t\tsend \"Be careful, you won't be able to pick that up!\" to player"
})
@Since("INSERT VERSION")
public class CondCanPickUpItems extends PropertyCondition<LivingEntity> {

	static {
		register(CondCanPickUpItems.class, PropertyType.CAN, "pick([ ]up items| items up)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		return livingEntity.getCanPickupItems();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "pick up items";
	}

}
