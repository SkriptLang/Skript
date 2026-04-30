package org.skriptlang.skript.bukkit.world.boarder.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import org.bukkit.event.Event;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

import javax.annotation.Nullable;

public class EvtWorldBoarder extends SkriptEvent {

	public static void register(SyntaxRegistry registry) {
		if (Skript.classExists("io.papermc.paper.event.world.border.WorldBorderEvent")) {
			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtWorldBoarder.class, "World Border Bounds Change")
					.addEvent(WorldBorderBoundsChangeEvent.class)
					.addPatterns("world[ ]border [bounds] chang(e|ing)")
					.addDescription(
						"Called when a world border changes its bounds, either over time, or instantly.",
						"This event does not get called for virtual borders."
					)
					.addExample("""
						on worldborder bounds change:
							broadcast "The diameter of %event-worldborder% is changing from %past event-number% to %event-number% over the next %event-timespan%"
						""")
					.addSince("2.11")
					.supplier(EvtWorldBoarder::new)
					.build()
			);

			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtWorldBoarder.class, "World Border Bounds Finish Change")
					.addEvent(WorldBorderBoundsChangeFinishEvent.class)
					.addPatterns("world[ ]border [bounds] finish chang(e|ing)")
					.addDescription(
						"Called when a moving world border has finished its move.",
						"This event does not get called for virtual borders."
					)
					.addExample("""
						on worldborder bounds finish change:
							broadcast "Over the past %event-timespan%, the diameter of %event-worldborder% went from %past event-number% to %event-number%"
						""")
					.addSince("2.11")
					.supplier(EvtWorldBoarder::new)
					.build()
			);

			registry.register(
				BukkitSyntaxInfos.Event.KEY,
				BukkitSyntaxInfos.Event.builder(EvtWorldBoarder.class, "World Border Center Change")
					.addEvent(WorldBorderCenterChangeEvent.class)
					.addPatterns("world[ ]border center chang(e|ing)")
					.addDescription(
						"Called when a world border's center has changed.",
						"This event does not get called for virtual borders."
					)
					.addExample("""
						on worldborder center change:
							broadcast "The center of %event-worldborder% has moved from %past event-location% to %event-location%"
						""")
					.addSince("2.11")
					.supplier(EvtWorldBoarder::new)
					.build()
			);
		}
	}

	@Override
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) { return "world boarder event"; }
}
