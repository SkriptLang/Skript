package org.skriptlang.skript.bukkit.command.elements.events;

import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.event.command.UnknownCommandEvent;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtUnknownCommand extends SimpleEvent {

	public EvtUnknownCommand() {
		super("unknown command execution");
	}

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY,
			Event.builder(EvtUnknownCommand.class, "Unknown Command Execution")
				.supplier(EvtUnknownCommand::new)
				.addEvent(UnknownCommandEvent.class)
				.addPattern("(unknown|non-existent) command [execution]")
				.addDescription("""
					Called when a player executes an unknown command.
					""")
				.addExample("""
					on unknown command execution:
						add 1 to {-command_fails::%player%}
						wait 30 seconds:
							remove 1 from {-command_fails::%player%}
						if {-command_fails::%player%} is greater than or equal to 5:
							push the player upwards
							set the unknown command message to "<red>You are executing too many unknown commands too fast!"
					""")
				.addSince("INSERT VERSION")
				.build());
		eventValueRegistry.register(EventValue.simple(UnknownCommandEvent.class, CommandSender.class, UnknownCommandEvent::getSender));
		eventValueRegistry.register(EventValue.simple(UnknownCommandEvent.class, Block.class,
			event -> event.getSender() instanceof BlockCommandSender sender ? sender.getBlock() : null));
		eventValueRegistry.register(EventValue.simple(UnknownCommandEvent.class, Location.class,
			event -> event.getCommandSource().getLocation()));
		eventValueRegistry.register(EventValue.simple(UnknownCommandEvent.class, World.class,
			event -> event.getCommandSource().getLocation().getWorld()));
	}

}
