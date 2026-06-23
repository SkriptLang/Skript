package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.variables.Variables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.List;

public class SkriptCommandExecutor {

	private final Trigger trigger;
	private final List<ArgumentData<?>> arguments;

	public SkriptCommandExecutor(Trigger trigger, List<ArgumentData<?>> arguments) {
		this.trigger = trigger;
		this.arguments = arguments;
	}

	public int execute(CommandContext<CommandSourceStack> context, int argCount) {
		CommandEvent commandEvent = new CommandEvent(context.getSource().getSender());

		int i = 0;
		for (ArgumentData<?> argument : arguments) {
			if (argument.isAutomaticName()) {
				continue;
			}
			Object value = null;
			if (i < argCount) {
				value = context.getArgument(argument.name(), argument.type().getC());
			} else if (argument.defaultValue() != null) {
				value = argument.defaultValue().getSingle(commandEvent);
			}
			Variables.setVariable(argument.name(), value, commandEvent, true);
			i++;
		}
		trigger.execute(commandEvent);

		return Command.SINGLE_SUCCESS;
	}

}
