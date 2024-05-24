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

import org.bukkit.entity.Entity;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.util.Validated;

@Name("Is Valid")
@Description({
	"Checks whether something (an entity, a script) is valid.",
	"An invalid entity may have died or de-spawned for some other reason.",
	"An invalid script reference may have been reloaded, moved or disabled since."
})
@Examples("if event-entity is valid")
@Since("2.7, INSERT VERSION (Scripts)")
public class CondIsValid extends PropertyCondition<Object> {

	static {
		register(CondIsValid.class, "valid", "entities/scripts");
	}

	@Override
	public boolean check(Object value) {
		if (value instanceof Entity)
			return ((Entity) value).isValid();
		if (value instanceof Validated)
			return ((Validated) value).valid();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "valid";
	}

}
