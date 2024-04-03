package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.command.UnknownCommandEvent;

import javax.annotation.Nullable;

public class EvtUnknownCommand extends SkriptEvent {

	static {
		Skript.registerEvent("unknown command", SimpleEvent.class, UnknownCommandEvent.class,
			"[player] (wrong|unknown) (cmd|command) (use|send)");
		EventValues.registerEventValue(UnknownCommandEvent.class, CommandSender.class, new Getter<>() {
			@Override
			public CommandSender get(UnknownCommandEvent e) {
				return e.getSender();
			}
		}, 0);

		EventValues.registerEventValue(UnknownCommandEvent.class, String.class, new Getter<String, UnknownCommandEvent>() {
			@Override
			public String get(UnknownCommandEvent e) {
				return e.getCommandLine();
			}
		}, 0);
	}

	@Override
	public boolean init(Literal<?>[] literals, int i, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	public boolean check(Event event) {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean b) {
		return "player unknown command event";
	}
}
