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

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;

public final class EvtEntity extends SkriptEvent {

	public static final String PLAYER_DEATH_EVENT_NAME = "Player Death";

	static {
		// Must be registered before EntityDeathEvent. Also SimpleEvent is correct.
		Skript.registerEvent(PLAYER_DEATH_EVENT_NAME, SimpleEvent.class, PlayerDeathEvent.class, "death of [a[n]] player", "player death")
				.description("Called when a player dies.")
				.examples("on death of player:",
						"\tbroadcast \"%player% has been slain in %world%!\"")
				.since("1.0");

		Skript.registerEvent("Death", EvtEntity.class, EntityDeathEvent.class, "death [of %-entitydatas%]")
				.description("Called when a living entity (including players) dies.")
				.examples("on death:",
						"on death of player:",
						"on death of a wither or ender dragon:",
						"	broadcast \"A %entity% has been slain in %world%!\"")
				.since("1.0");
		Skript.registerEvent("Spawn", EvtEntity.class, EntitySpawnEvent.class, "spawn[ing] [of %-entitydatas%]")
				.description("Called when an entity spawns (excluding players).")
				.examples("on spawn of a zombie:",
						"on spawn of an ender dragon:",
						"	broadcast \"A dragon has been sighted in %world%!\"")
				.since("1.0, 2.5.1 (non-living entities)");
	}

	@Nullable
	private EntityData<?>[] types;
	private boolean spawn;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		types = args[0] == null ? null : ((Literal<EntityData<?>>) args[0]).getAll();
		spawn = StringUtils.startsWithIgnoreCase(parser.expr, "spawn");
		if (types != null) {
			if (spawn) {
				for (EntityData<?> data : types) {
					if (HumanEntity.class.isAssignableFrom(data.getType())) {
						Skript.error("The spawn event does not work for human entities");
						return false;
					}
				}
			} else {
				for (EntityData<?> data : types) {
					if (!LivingEntity.class.isAssignableFrom(data.getType())) {
						Skript.error("The death event only works for living entities");
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (types == null)
			return true;
		Entity entity = event instanceof EntityDeathEvent ? ((EntityDeathEvent) event).getEntity() : ((EntitySpawnEvent) event).getEntity();
		for (EntityData<?> data : types) {
			if (data.isInstance(entity))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (spawn ? "spawn" : "death") + (types != null ? " of " + Classes.toString(types, false) : "");
	}

}
