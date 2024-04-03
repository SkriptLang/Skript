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
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;

public class EvtEntityPotion extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Potion Effect", EvtEntityPotion.class, EntityPotionEffectEvent.class,
				"add[ing] [of] [entity] potion effect [[of] %potioneffecttypes%]",
				"remov[e|ing] [of] [entity] potion effect [[of] %potioneffecttypes%]",
				"clear[ing] [of] [entity] potion effect [[of] %potioneffecttypes%]",
				"chang[e|ing] [of] [entity] potion effect [[of] %potioneffecttypes%]")
			.description("Called when an entity's potion effect is modified.", "This modification can include adding, removing or changing their potion effect.")
			.examples("on adding potion effect:", "on removing potion effect:", "on changing potion effect:", "on adding potion effect night vision:")
			.since("INSERT VERSION");
	}

	@SuppressWarnings("unchecked")
	private Expression<PotionEffectType> potionEffects;
	private int matchedPattern;

	private EntityPotionEffectEvent.Action action;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		potionEffects = (Expression<PotionEffectType>) args[0];
		switch (matchedPattern) {
			case 0:
				action = EntityPotionEffectEvent.Action.ADDED;
				break;
			case 1:
				action = EntityPotionEffectEvent.Action.REMOVED;
				break;
			case 2:
				action = EntityPotionEffectEvent.Action.CHANGED;
				break;
		}
		return true;
	}

	@Override
	public boolean check(Event e) {
		if (e instanceof EntityPotionEffectEvent) {
			EntityPotionEffectEvent event = (EntityPotionEffectEvent) e;
			if (event.getAction() == action) {
				if (potionEffects != null && event.getNewEffect() != null) {
					PotionEffectType effectType = event.getNewEffect().getType();
					for (PotionEffectType potionEffectType : potionEffects.getArray(e)) {
						if (potionEffectType.equals(effectType)) {
							return true;
						}
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "on entity potion effect";
	}
}
