package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.variables.Variables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An executor for Brigadier commands.
 */
@ApiStatus.Internal
public class ScriptCommandExecutor {

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
	private final @Nullable CooldownManager cooldownManager;

	public ScriptCommandExecutor(Trigger trigger, List<ArgumentData<?>> arguments, @Nullable CooldownManager cooldownManager) {
		this.trigger = trigger;
		this.arguments = arguments;
		this.cooldownManager = cooldownManager;
	}

	public @Nullable CooldownManager getCooldownManager() {
		return cooldownManager;
	}

	public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSender sender = context.getSource().getSender();
		ScriptCommandEvent commandEvent =
			new ScriptCommandEvent(context.getNodes().getFirst().getNode().getName(), context.getInput(), sender, this);

		// final validations
		if (cooldownManager != null && !cooldownManager.checkExecution(commandEvent, sender)) {
			return Command.SINGLE_SUCCESS;
		}

		// argument assembly
		Set<String> providedArgs = context.getNodes().stream()
			.filter(node -> node.getNode() instanceof ArgumentCommandNode<?,?>)
			.map(node -> node.getNode().getName())
			.collect(Collectors.toSet());
		for (ArgumentData<?> argument : arguments) {
			Object value = null;
			if (providedArgs.contains(argument.name())) {
				value = context.getArgument(argument.name(), Object.class); // we manually handle type validation
				if (value instanceof ArgumentResolver<?> argumentResolver) { // native type needs resolved
					value = argumentResolver.resolve(context.getSource());
					if (value instanceof SequencedCollection<?> collection) {
						if (argument.isSingle()) { // many single arguments are still provided as lists
							value = collection.getFirst();
						} else {
							value = collection.toArray((Object[]) Array.newInstance(argument.type().getC(), collection.size()));
						}
					}
				}
			} else if (argument.defaultValue() != null) { // fallback to default value
				if (argument.isSingle()) {
					value = argument.defaultValue().getSingle(commandEvent);
				} else {
					value = argument.defaultValue().getArray(commandEvent);
				}
			}

			if (value != null) {
				commandEvent.arguments.put(argument.name(), value);
				if (!argument.isAutomaticName()) { // store explicitly named arguments as variables
					setVariable(argument.name(), value, argument.isSingle(), commandEvent);
				}
			}
		}

		// execution
		trigger.execute(commandEvent);

		return Command.SINGLE_SUCCESS;
	}

}
