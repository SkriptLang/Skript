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
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Custom Name Visible")
@Description("Check if an entity's custom name is visible.")
@Examples({
	"send true if target's custom name is visible"
})
@Since("INSERT VERSION")
public class CondIsCustomNameVisible extends PropertyCondition<Entity> {

	static {
		Skript.registerCondition(CondIsCustomNameVisible.class,
			"%entities%'s custom name (:is|isn't) visible",
			"custom name of %entities% (:is|isn't) visible");
	}

	@Override
	public boolean check(Entity value) {
		return value.isCustomNameVisible() && !isNegated();
	}

	@Override
	protected String getPropertyName() {
		return "custom name";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "custom name";
	}


}
