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

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;

@Name("Can Breed")
@Description("Returns whether or not a living entity is breedable.")
@Examples({
	"on right click on living entity with bucket:",
		"\tevent-entity can't breed",
		"\tsend \"Turns out %event-entity% is not breedable, what a let down\" to player"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.16+")
public class CondCanBreed extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.classExists("org.bukkit.entity.Breedable"))
			register(CondCanBreed.class, PropertyType.CAN, "breed", "livingentities");
	}

	@Override
	public boolean check(LivingEntity livingEntity) {
		if (livingEntity instanceof Breedable)
			return ((Breedable) livingEntity).canBreed();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "breed";
	}

}
