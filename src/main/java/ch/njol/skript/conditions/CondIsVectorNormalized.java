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
import org.bukkit.util.Vector;

@Name("Is Normalized")
@Description("Checks whether a vector is normalized i.e. length of 1")
@Examples("vector of player's location is normalized")
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.13.2+")
public class CondIsVectorNormalized extends PropertyCondition<Vector> {

	static {
		if (Skript.methodExists(Vector.class, "isNormalized")) {
			register(CondIsVectorNormalized.class, "normalized", "vectors");
		}
	}

	@Override
	public boolean check(Vector vector) {
		return vector.isNormalized();
	}

	@Override
	protected String getPropertyName() {
		return "normalized";
	}

}
