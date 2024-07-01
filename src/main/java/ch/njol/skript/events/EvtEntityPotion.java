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
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;

public class EvtEntityPotion extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Potion Effect", EvtEntityPotion.class, EntityPotionEffectEvent.class,
				"entity potion effect [modif[y|ication]] [[of] %-potioneffecttypes%] [due to %-entitypotioncause%]")
			.description("Called when an entity's potion effect is modified.", "This modification can include adding, removing or changing their potion effect.")
			.examples(
				"on entity potion effect modification:",
					"\t\tbroadcast \"A potion effect was added to %event-entity%!\" ",
				"",
				"on entity potion effect modification of night vision:")
			.since("INSERT VERSION");
	}

	@SuppressWarnings("unchecked")
	private Expression<PotionEffectType> potionEffects;
	private Expression<EntityPotionEffectEvent.Cause> cause;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		potionEffects = (Expression<PotionEffectType>) args[0];
		cause = (Expression<EntityPotionEffectEvent.Cause>) args[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		EntityPotionEffectEvent potionEvent = (EntityPotionEffectEvent) event;
		boolean effectMatches = potionEffects == null ||
			(potionEvent.getOldEffect() != null && potionEffects.check(event, effectType -> effectType.equals(potionEvent.getOldEffect().getType()))) ||
			(potionEvent.getNewEffect() != null && potionEffects.check(event, effectType -> effectType.equals(potionEvent.getNewEffect().getType())));

		boolean causeMatches = cause == null || cause.check(event, cause -> cause.equals(potionEvent.getCause()));

		return effectMatches && causeMatches;
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "on entity potion effect modification";
	}
}
