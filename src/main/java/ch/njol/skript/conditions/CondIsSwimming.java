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
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import org.bukkit.entity.LivingEntity;

@Name("Is Swimming")
@Description("Checks whether a living entity is swimming.")
@Examples("player is swimming")
@RequiredPlugins("1.13 or newer")
@Since("2.3")
public class CondIsSwimming extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(LivingEntity.class, "isSwimming"))
			register(CondIsSwimming.class, "swimming", "livingentities");
	}

	@Override
	public boolean check(final LivingEntity e) {
		return e.isSwimming();
	}

	@Override
	protected String getPropertyName() {
		return "swimming";
	}

}
