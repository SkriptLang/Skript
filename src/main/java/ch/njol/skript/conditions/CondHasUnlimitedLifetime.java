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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

@Name("Item Has Unlimited Lifetime")
@Description("Checks whether the given entities are an item with an unlimited lifetime.")
@Examples("if last dropped item has an unlimited lifetime:")
@Since("INSERT VERSION")
public class CondHasUnlimitedLifetime extends PropertyCondition<Entity> {

	static {
		register(CondHasUnlimitedLifetime.class, PropertyType.HAVE, "[an] (unlimited|infinite) life(time|span)", "entities");
	}

	@Override
	public boolean check(Entity entity) {
		if (!(entity instanceof Item))
			return false;
		return ((Item) entity).isUnlimitedLifetime();
	}

	@Override
	protected String getPropertyName() {
		return "unlimited lifetime";
	}

}
