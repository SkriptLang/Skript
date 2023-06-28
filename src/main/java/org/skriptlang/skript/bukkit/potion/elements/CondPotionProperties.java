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
package org.skriptlang.skript.bukkit.potion.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

@Name("Potion Properties")
@Description("Checks whether a potion effect has a certain property such as an infinite duration or particles.")
@Examples({
	"{_potionEffect} has an icon",
	"a random element out of the active potion effects of the player is infinite"
})
@Since("INSERT VERSION")
public class CondPotionProperties extends PropertyCondition<SkriptPotionEffect> {

	static {
		register(CondPotionProperties.class, PropertyType.BE, "(AMBIENT:ambient|INFINITE:infinite)", "potioneffects");
		register(CondPotionProperties.class, PropertyType.HAVE, "(ICON:(an icon|icons)|PARTICLES:particles)", "potioneffects");
	}

	private enum Property {
		INFINITE,
		AMBIENT,
		PARTICLES,
		ICON
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Property property;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		property = Property.valueOf(parseResult.tags.get(0));
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(SkriptPotionEffect potionEffect) {
		switch (property) {
			case AMBIENT:
				return potionEffect.ambient();
			case ICON:
				return potionEffect.icon();
			case INFINITE:
				return potionEffect.infinite();
			case PARTICLES:
				return potionEffect.particles();
			default:
				throw new IllegalArgumentException("Invalid Potion Property: " + property);
		}
	}

	@Override
	protected PropertyType getPropertyType() {
		switch (property) {
			case AMBIENT:
			case INFINITE:
				return PropertyType.BE;
			case ICON:
			case PARTICLES:
				return PropertyType.HAVE;
			default:
				throw new IllegalArgumentException("Invalid Potion Property: " + property);
		}
	}

	@Override
	protected String getPropertyName() {
		switch (property) {
			case AMBIENT:
				return "ambient";
			case ICON:
				return "an icon";
			case INFINITE:
				return "infinite";
			case PARTICLES:
				return "particles";
			default:
				throw new IllegalArgumentException("Invalid Potion Property: " + property);
		}
	}

}
