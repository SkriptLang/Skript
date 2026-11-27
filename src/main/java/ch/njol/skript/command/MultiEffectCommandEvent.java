package ch.njol.skript.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

/**
 * {@link Event} for indicating a multiline effect command being executed.
 */
public class MultiEffectCommandEvent extends EffectCommandEvent {

	public MultiEffectCommandEvent(CommandSender sender, String command) {
		super(sender, command);
	}

}
