package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.Statistic;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.eclipse.jdt.annotation.Nullable;

public class EvtJump extends SkriptEvent {

    static {
        Skript.registerEvent("Jump", EvtJump.class, PlayerStatisticIncrementEvent.class, "[player] jump[ing]")
					.description("Called whenever a player jumps.")
					.examples(
							"on jump:",
							"\tevent-player does not have permission \"jump\"",
							"\tcancel event"
					).since("2.3 (Paper), INSERT VERSION (Using Statistics)")
    }


    @Override
    public boolean init(Literal[] args, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(Event e) {
        return (((PlayerStatisticIncrementEvent) e).getStatistic() == Statistic.JUMP);
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "player jump event";
    }

}
