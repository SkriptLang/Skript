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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class EvtEntityTransform extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Transform", EvtEntityTransform.class, EntityTransformEvent.class, "(entit(y|ies)|%*-entitydatas%) transform[ing] [due to %-transformreasons%]")
				.description("Called when an entity is about to be replaced by another entity.",
						"Examples when it's called include; when a zombie gets cured and a villager spawns, " +
						"an entity drowns in water like a zombie that turns to a drown, " +
						"an entity that gets frozen in powder snow, " +
						"a mooshroom that when sheared, spawns a new cow.")
				.examples("on a zombie transforming due to curing:", "on mooshroom transforming:", "on zombie, skeleton or slime transform:")
				.keywords("entity transform")
				.since("2.8.0");
	}

	@Nullable
	private Literal<TransformReason> reasons;

	@Nullable
	private Literal<EntityData<?>> datas;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		datas = (Literal<EntityData<?>>) args[0];
		reasons = (Literal<TransformReason>) args[1];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityTransformEvent))
			return false;
		EntityTransformEvent transformEvent = (EntityTransformEvent) event;
		if (reasons != null && !reasons.check(event, reason -> transformEvent.getTransformReason().equals(reason)))
			return false;
		if (datas != null && !datas.check(event, data -> data.isInstance(transformEvent.getEntity())))
			return false;
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (datas == null)
			return "entities transforming" + (reasons == null ? "" : " due to " + reasons.toString(event, debug));
		return datas.toString(event, debug) + " transforming" + (reasons == null ? "" : " due to " + reasons.toString(event, debug));
	}

}
