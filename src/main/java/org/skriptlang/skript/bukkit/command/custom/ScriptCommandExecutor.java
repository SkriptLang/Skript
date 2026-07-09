package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.variables.Variables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An executor for Brigadier commands.
 */
@ApiStatus.Internal
public class ScriptCommandExecutor {

	/**
	 * Utility method for mapping a list of {@link ArgumentData}s to their real values given context.
	 * @param arguments Arguments to map to values.
	 * @param context Command context for obtaining argument values.
	 * @param commandEvent Event context for resolving default values.
	 * @return A map of {@code arguments} to their values.
	 *  Arguments that fail to resolve to a value are not included.
	 * @throws CommandSyntaxException If an error occurs while resolving certain arguments.
	 */
	public static Map<ArgumentData<?>, Object> getArguments(List<ArgumentData<?>> arguments, CommandContext<CommandSourceStack> context,
	                                                        ScriptCommandEvent commandEvent) throws CommandSyntaxException {
		Map<ArgumentData<?>, Object> argumentMap = new HashMap<>();
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
				argumentMap.put(argument, value);
			}
		}
		return argumentMap;
	}

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

	@ApiStatus.Internal
	public ScriptCommandExecutor(Trigger trigger, List<ArgumentData<?>> arguments, @Nullable CooldownManager cooldownManager) {
		this.trigger = trigger;
		this.arguments = arguments;
		this.cooldownManager = cooldownManager;
	}

	public @Nullable CooldownManager getCooldownManager() {
		return cooldownManager;
	}

	public int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ScriptCommandEvent commandEvent =
			new ScriptCommandEvent(context.getNodes().getFirst().getNode().getName(), context.getInput(), this, source);

		// final validations
		if (cooldownManager != null && !cooldownManager.checkExecution(commandEvent, source.getSender())) {
			return Command.SINGLE_SUCCESS;
		}

		// parse and store arguments
		Map<ArgumentData<?>, Object> arguments = getArguments(this.arguments, context, commandEvent);
		arguments.forEach((data, value) -> {
			commandEvent.arguments.put(data.name(), value);
			if (!data.isAutomaticName()) { // store explicitly named arguments as variables
				setVariable(data.name(), value, data.isSingle(), commandEvent);
			}
		});

		// execution
		trigger.execute(commandEvent);

		return Command.SINGLE_SUCCESS;
	}

}
