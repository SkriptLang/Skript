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

public class EvtPlayerStatisticIncrease extends SkriptEvent {

	static {
		Skript.registerEvent("Player Statistic Increase", EvtPlayerStatisticIncrease.class, PlayerStatisticIncrementEvent.class,
			"player statistic increase [of %*-strings%]")
			.description("Called when a player's statistic increases.")
			.examples(
				"on player statistic increase:",
					"\tbroadcast \"%player%'s statistic '%event-statistic%' increased! They now have done that %future statistic value% times!\"")
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

	private Literal<String> statistics;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		statistics = (Literal<String>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (statistics != null) {
			Statistic statistic = ((PlayerStatisticIncrementEvent) event).getStatistic();

			for (String string : this.statistics.getAll(event)) {
				if (statistic.name().equalsIgnoreCase(string.replace(' ', '_')))
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
