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
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

public class EvtStatisticChange extends SkriptEvent {

	static {
		Skript.registerEvent("Statistic Change", EvtStatisticChange.class, PlayerStatisticIncrementEvent.class,
				"[player] statistic[s] (change|increase) [of %string%]")
				.description("Called when a player statistic is incremented.",
						"This event is not called for some high frequency statistics, e.g. movement based statistics.",
						"You can use past/future event-number to the get the past/future new value, event-number is the difference between the old and the new value."
				)
				.examples(
						"on player statistic increase:",
						"\tsend \"Statistic increased: %event-string% from %past event-number% to %future event-number% (diff: %event-number%)\" to player",
						"\tif event-entitytype is set:",
						"\t\tsend \"Of entity: %event-entitytype%\" to player",
						"\telse if event-item is set:",
						"\t\tsend \"Of item: %event-item%\" to player",
						"on player statistic increase of \"CHEST_OPENED\":",
						"\tsend \"You just opened a chest!\" to player",
						"",
						"# 'TIME_SINCE_REST' is called too many times therefore it will not trigger the event unless specified like below",
						"on player statistic increase of \"TIME_SINCE_REST\":",
						"\tsend \"Your chat is being flooded\" to player"
				)
				.since("INSERT VERSION");

		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, String.class, new Getter<String, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public String get(PlayerStatisticIncrementEvent e) {
				return e.getStatistic().toString();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, Number.class, new Getter<Number, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public Number get(PlayerStatisticIncrementEvent e) {
				return e.getPreviousValue();
			}
		}, EventValues.TIME_PAST);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, Number.class, new Getter<Number, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public Number get(PlayerStatisticIncrementEvent e) {
				return e.getNewValue() - e.getPreviousValue();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, Number.class, new Getter<Number, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public Number get(PlayerStatisticIncrementEvent e) {
				return e.getNewValue();
			}
		}, EventValues.TIME_FUTURE);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, EntityData.class, new Getter<EntityData, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public EntityData get(PlayerStatisticIncrementEvent e) {
				Class<? extends Entity> clazz = e.getEntityType() != null ? e.getEntityType().getEntityClass() : null;
				return clazz == null ? null : EntityData.fromClass(clazz);
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, ItemStack.class, new Getter<ItemStack, PlayerStatisticIncrementEvent>() {
			@Nullable
			@Override
			public ItemStack get(PlayerStatisticIncrementEvent e) {
				return e.getMaterial() == null ? null : new ItemStack(e.getMaterial());
			}
		}, EventValues.TIME_NOW);
	}

	@Nullable
	private Statistic statistic;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
		Literal<String> stat = (Literal<String>) args[0];
		if (stat != null) {
			try {
				statistic = Statistic.valueOf(stat.getSingle().toUpperCase());
			} catch (IllegalArgumentException ex) {
				Skript.error("Unknown statistic name used in statistic change event");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		// TIME_SINCE_REST is called too many times therefore we will not make it trigger unless specified
		return (statistic == null && ((PlayerStatisticIncrementEvent) event).getStatistic() != Statistic.TIME_SINCE_REST) ||
			((PlayerStatisticIncrementEvent) event).getStatistic() == statistic;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "statistic increase" + (statistic != null ? " of " + statistic : "");
	}
	
}
