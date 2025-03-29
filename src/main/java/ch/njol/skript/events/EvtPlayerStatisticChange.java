package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.Statistic;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerStatisticChange extends SkriptEvent {

	static {
		Skript.registerEvent("Player Statistic Change", EvtPlayerStatisticChange.class, PlayerStatisticIncrementEvent.class,
			"player statistic (change|increase|increment) [of %-statistics% [statistic[s]]]",
				"player statistic (change|increase|increment) [of [statistic[s]] %-statistics%]")
			.description(
				"Called when a player's statistic changes. Some statistics like 'play one minute' do not call this event, "
					+ "because they get called too often.")
			.examples(
				"on player statistic increase:",
					"\tbroadcast \"%player%'s statistic '%event-statistic%' increased! It is now %future statistic value%!\"",
				"on player statistic increase of leave game:",
					"\tbroadcast \"%player% left the game for %future statistic value% times..\"")
			.since("INSERT VERSION");

		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, Statistic.class, PlayerStatisticIncrementEvent::getStatistic);

		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, ItemType.class, event -> {
			if (event.getMaterial() == null)
				return null;
			return new ItemType(event.getMaterial());
		});
		EventValues.registerEventValue(PlayerStatisticIncrementEvent.class, EntityData.class, event -> {
			if (event.getEntityType() == null)
				return null;
			return EntityUtils.toSkriptEntityData(event.getEntityType());
		});
	}

	private Literal<Statistic> statistics;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		statistics = (Literal<Statistic>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (statistics != null) {
			Statistic statistic = ((PlayerStatisticIncrementEvent) event).getStatistic();

			for (Statistic value : this.statistics.getAll(event)) {
				if (statistic.equals(value))
					return true;
			}

			return false;
		}

		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "player statistic change" + (statistics != null ? (" of " + statistics.toString(event, debug)) : "");
	}

}
