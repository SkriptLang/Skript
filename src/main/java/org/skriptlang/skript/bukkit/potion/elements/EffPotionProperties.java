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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect.Property;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Properties")
@Description("Modify the properties of a potion such as whether it is infinite in duration or if the icon is shown.")
@Examples("hide the particles of haste for the player >= 3:")
@Since("INSERT VERSION")
public class EffPotionProperties extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffPotionProperties.class)
				.addPatterns(
						"(show|not:hide) [the] [potion] (ICON:icon|PARTICLES:particles) of %potioneffecttypes% (of|for|on) %livingentities%",
						"make %potioneffecttypes% (of|for|on) %livingentities% [:not] (AMBIENT:ambient|INFINITE:infinite)",
						"(show|not:hide) [the] [potion] (ICON:icon|PARTICLES:particles) for %potioneffects%",
						"make %potioneffects% [:not] (AMBIENT:ambient|INFINITE:infinite)"
				)
				.build()
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Property property;
	private boolean hide;

	@Nullable
	private Expression<PotionEffectType> types;
	@Nullable
	private Expression<LivingEntity> entities;
	@Nullable
	private Expression<SkriptPotionEffect> potions;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		property = Property.valueOf(parseResult.tags.get(parseResult.tags.size() - 1));
		hide = parseResult.hasTag("hide");
		if (matchedPattern == 0) {
			types = (Expression<PotionEffectType>) exprs[0];
			entities = (Expression<LivingEntity>) exprs[1];
		} else {
			potions = (Expression<SkriptPotionEffect>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {

	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String expression;
		if (potions != null) {
			expression = "for " + potions.toString(event, debug);
		} else {
			assert types != null && entities != null;
			expression = "of " + types.toString(event, debug) + " for " + entities.toString(event, debug);
		}

		switch (property) {
			case ICON:
			case PARTICLES:
				return (hide ? "hide" : "show") + " the potion " + property.displayName() + " " + expression;
			case AMBIENT:
			case INFINITE:
				return "make " + expression + (hide ? " not " : " ") + property.displayName();
			default:
				throw new IllegalArgumentException("Unexpected Potion Property: " + property);
		}
	}

}
