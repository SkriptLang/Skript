package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.variables.Variables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;

import java.util.List;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Collectors;

public class SkriptCommandExecutor {

	private final Trigger trigger;
	private final List<ArgumentData<?>> arguments;

	public SkriptCommandExecutor(Trigger trigger, List<ArgumentData<?>> arguments) {
		this.trigger = trigger;
		this.arguments = arguments;
	}

	public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandEvent commandEvent = new CommandEvent(context.getSource().getSender());

		Set<String> providedArgs = context.getNodes().stream()
			.filter(node -> node.getNode() instanceof ArgumentCommandNode<?,?>)
			.map(node -> node.getNode().getName())
			.collect(Collectors.toSet());
		for (ArgumentData<?> argument : arguments) {
			Object value = null;
			if (providedArgs.contains(argument.name())) {
				value = context.getArgument(argument.name(), Object.class);
				if (value instanceof ArgumentResolver<?> argumentResolver) { // native type needs resolved
					value = argumentResolver.resolve(context.getSource());
					if (value instanceof SequencedCollection<?> collection) {
						value = collection.getFirst();
					}
				} else if (value == SkriptBrigadierArgument.DEFAULT_PLACEHOLDER) {
					assert argument.defaultValue() != null;
					value = argument.defaultValue().getSingle(commandEvent);
				}
			} else if (argument.defaultValue() != null) {
				value = argument.defaultValue().getSingle(commandEvent);
			}
			Variables.setVariable(argument.name(), value, commandEvent, true);
		}
		trigger.execute(commandEvent);

		return Command.SINGLE_SUCCESS;
	}

}
