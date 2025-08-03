package ch.njol.skript.command.brigadier;

import ch.njol.skript.command.CommandEvent;
import com.mojang.brigadier.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event for executing brigadier commands.
 */
public class BrigadierCommandEvent extends CommandEvent {

	private final CommandContext<CommandSender> context;

	public BrigadierCommandEvent(CommandContext<CommandSender> context) {
		super(context.getSource(), context.getRootNode().getName(),
			context.getNodes().stream()
				.map(arg -> arg.getRange().get(context.getInput()))
				.toArray(String[]::new));
		this.context = context;
	}

	/**
	 * @return command execution context
	 */
	public CommandContext<CommandSender> getContext() {
		return context;
	}

	// Bukkit stuff
	private final static HandlerList handlers = new HandlerList();

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
