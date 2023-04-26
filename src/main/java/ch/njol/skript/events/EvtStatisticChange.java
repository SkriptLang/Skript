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
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.util.StringUtils;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvtStatisticChange extends SkriptEvent {

	static {
		Skript.registerEvent("Statistic Change", EvtStatisticChange.class, PlayerStatisticIncrementEvent.class,
				"[player] statistic[s] (change|increase) [of %statistics%]")
				.description("Called when a player statistic is incremented.",
						"You can use past/future event-number to the get the past/future new value, event-number is the difference between the old and the new value.",
						"NOTE: This event is not called for <a href='https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/event/CraftEventFactory.java#1456'>some high frequency statistics</a>, e.g. movement based statistics. However, note that the excluded statistics change from version to version, so you may find that some still work in some old versions, like 'TIME_SINCE_REST' before 1.17."
				)
				.examples(
						"on player statistic increase:",
							"\tsend \"Statistic increased: %event-string% from %past event-number% to %future event-number% (diff: %event-number%)\" to player",
							"\tif event-entitytype is set:",
								"\t\tsend \"Of entity: %event-entitytype%\" to player",
							"\telse if event-item is set:",
								"\t\tsend \"Of item: %event-item%\" to player",
						"",
						"on player statistic increase of \"CHEST_OPENED\":",
							"\tsend \"You just opened a chest!\" to player"
				)
				.since("INSERT VERSION");

		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, String.class, new Getter<String, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public String get(PlayerStatisticIncrementEvent event) {
				return event.getStatistic().toString();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, Number.class, new Getter<Number, PlayerStatisticIncrementEvent>() {
			@Override
			public Number get(PlayerStatisticIncrementEvent event) {
				return event.getPreviousValue();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, Number.class, new Getter<Number, PlayerStatisticIncrementEvent>() {
			@Override
			public Number get(PlayerStatisticIncrementEvent event) {
				return event.getNewValue() - event.getPreviousValue();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, Number.class, new Getter<Number, PlayerStatisticIncrementEvent>() {
			@Override
			public Number get(PlayerStatisticIncrementEvent event) {
				return event.getNewValue();
			}
		}, EventValues.TIME_FUTURE);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, EntityData.class, new Getter<EntityData, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public EntityData<?> get(PlayerStatisticIncrementEvent event) {
				Class<? extends Entity> c = event.getEntityType() != null ? event.getEntityType().getEntityClass() : null;
				return c == null ? null : EntityData.fromClass(c);
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, ItemStack.class, new Getter<ItemStack, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public ItemStack get(PlayerStatisticIncrementEvent event) {
				return event.getMaterial() == null ? null : new ItemStack(event.getMaterial());
			}
		}, EventValues.TIME_NOW);
	}

	@Nullable
	private Literal<Statistic> statistics;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		statistics = (Literal<Statistic>) args[0];
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		Statistic statistic = ((PlayerStatisticIncrementEvent) event).getStatistic();

		if (statistics == null)
			return true;

		return statistics.check(event, stat -> stat == statistic);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "statistic increase" + (statistics != null ? " of " + statistics.toString(event, debug) : "");
	}
	
}
