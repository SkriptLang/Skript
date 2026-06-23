package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.variables.Variables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;

import java.util.List;
import java.util.SequencedCollection;

public class SkriptCommandExecutor {

	private final Trigger trigger;
	private final List<ArgumentData<?>> arguments;

	public SkriptCommandExecutor(Trigger trigger, List<ArgumentData<?>> arguments) {
		this.trigger = trigger;
		this.arguments = arguments;
	}

	public int execute(CommandContext<CommandSourceStack> context, int argCount) throws CommandSyntaxException {
		CommandEvent commandEvent = new CommandEvent(context.getSource().getSender());

		int i = 0;
		for (ArgumentData<?> argument : arguments) {
			if (argument.isAutomaticName()) {
				continue;
			}
			Object value = null;
			if (i < argCount) {
				value = context.getArgument(argument.name(), Object.class);
				if (value instanceof ArgumentResolver<?> argumentResolver) {
					value = argumentResolver.resolve(context.getSource());
					if (value instanceof SequencedCollection<?> collection) {
						value = collection.getFirst();
					}
				}
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
