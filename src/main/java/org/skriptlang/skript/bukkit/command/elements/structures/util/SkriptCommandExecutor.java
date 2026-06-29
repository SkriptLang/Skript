package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.variables.Variables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import org.bukkit.event.Event;

import java.lang.reflect.Array;
import java.util.List;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An executor for Brigadier commands.
 */
class SkriptCommandExecutor {

	private static void setVariable(String name, Object value, boolean isSingle, Event context) {
		if (isSingle) {
			Variables.setVariable(name, value, context, true);
		} else {
			Object[] values = ((Object[]) value);
			int length = values.length;
			for (int i = 0; i < length; i++) {
				Variables.setVariable(name + "::" + (i + 1), values[i], context, true);
			}
		}
	}

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
						if (argument.isSingle()) {
							value = collection.getFirst();
						} else {
							value = collection.toArray((Object[]) Array.newInstance(argument.type().getC(), collection.size()));
						}
					}
				} else if (value == SkriptBrigadierArgument.DEFAULT_VALUE_PLACEHOLDER) {
					assert argument.defaultValue() != null;
					if (argument.isSingle()) {
						value = argument.defaultValue().getSingle(commandEvent);
					} else {
						value = argument.defaultValue().getArray(commandEvent);
					}
				}
			} else if (argument.defaultValue() != null) {
				if (argument.isSingle()) {
					value = argument.defaultValue().getSingle(commandEvent);
				} else {
					value = argument.defaultValue().getArray(commandEvent);
				}
			}

			if (value != null) {
				commandEvent.arguments.put(argument, value);
				if (!argument.isAutomaticName()) { // store explicitly named arguments as variables
					setVariable(argument.name(), value, argument.isSingle(), commandEvent);
				}
			}
		}
		trigger.execute(commandEvent);

		return Command.SINGLE_SUCCESS;
	}

}
