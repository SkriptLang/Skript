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

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class EvtEntityShootBow extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Shoot Bow", EvtEntityShootBow.class, EntityShootBowEvent.class, "[entity|%*-entitydatas%] shoot[ing [a]] bow")
				.description("Called when a living entity shoots a bow.")
				.examples("on skeleton shooting a bow:",
						"\tset the projectile to a primed tnt")
				.since("INSERT VERSION");
	}

	@Nullable
	private Literal<EntityData<?>> entityDatas;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] literals, int matchedPattern, ParseResult parser) {
		entityDatas = (Literal<EntityData<?>>) literals[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (entityDatas == null)
			return true;
		LivingEntity entity = ((EntityShootBowEvent) event).getEntity();
		return entityDatas.check(event, entityData -> entityData.isInstance(entity));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (entityDatas == null ? "entity " : entityDatas.toString(event, debug)) + " shooting a bow";
	}

}
