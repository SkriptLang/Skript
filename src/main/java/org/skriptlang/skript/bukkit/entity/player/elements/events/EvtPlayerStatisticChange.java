package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.Statistic;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue.Time;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static ch.njol.skript.bukkitutil.EntityUtils.toSkriptEntityData;

@SuppressWarnings("unchecked")
public class EvtPlayerStatisticChange extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry registry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerStatisticChange.class, "Player Statistic Change")
			.supplier(EvtPlayerStatisticChange::new)
			.addEvent(PlayerStatisticIncrementEvent.class)
			.addPatterns("player statistic (change|increase|increment) [of %-statistics% stat[istic][s]]")
			.addDescription("""
				Called when a player's statistic changes.
				Some statistics like 'play one minute' do not call this event, because they get called too often.
				This event is only called when the server updates a statistic and as such does not call if the statistic expression is used.
				""")
			.addExample("""
				on player statistic increase:
					broadcast "%player%'s statistic '%event-statistic%' increased! It is now %event-number%!
				""")
			.addExample("""
				on player statistic increase of leave game:
					broadcast "%player% has now left the game %event-number% times!"
				""")
			.addSince("INSERT VERSION")
			.build());

		registry.register(EventValue.builder(PlayerStatisticIncrementEvent.class, Integer.class)
			.getter(PlayerStatisticIncrementEvent::getNewValue)
			.build());

		registry.register(EventValue.builder(PlayerStatisticIncrementEvent.class, Integer.class)
			.getter(PlayerStatisticIncrementEvent::getPreviousValue)
			.time(Time.PAST)
			.build());

		registry.register(EventValue.builder(PlayerStatisticIncrementEvent.class, Statistic.class)
			.getter(PlayerStatisticIncrementEvent::getStatistic)
			.build());

		registry.register(EventValue.builder(PlayerStatisticIncrementEvent.class, ItemType.class)
			.getter(event -> new ItemType(event.getMaterial()))
			.build());

		registry.register(EventValue.builder(PlayerStatisticIncrementEvent.class, EntityData.class)
			.getter(event -> toSkriptEntityData(event.getEntityType()))
			.build());
	}

	private Literal<Statistic> statistics;

	@Override
	public boolean init(Literal<?>[] literals, int i, ParseResult parseResult) {
		statistics = (Literal<Statistic>) literals[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (statistics == null)
			return true;
		Statistic statistic = ((PlayerStatisticIncrementEvent) event).getStatistic();
		for (Statistic value : this.statistics.getAll(event)) {
			if (statistic.equals(value))
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("player statistic change")
			.appendIf(statistics != null, "of", statistics)
			.toString();
	}

}
