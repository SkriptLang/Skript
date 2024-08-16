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

import org.bukkit.potion.PotionEffect;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

// This class can be expanded apon for other types if needed.
@Name("Is Infinite")
@Description("Checks whether potion effects are infinite.")
@Examples("all of the active potion effects of the player are infinite")
@Since("2.7")
public class CondIsInfinite extends PropertyCondition<PotionEffect> {

	static {
		if (Skript.methodExists(PotionEffect.class, "isInfinite"))
			register(CondIsInfinite.class, "infinite", "potioneffects");
	}

	@Override
	public boolean check(PotionEffect potion) {
		return potion.isInfinite();
	}

	@Override
	protected String getPropertyName() {
		return "infinite";
	}

}
