package org.skriptlang.skript.lang.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;

import java.util.Map;
import java.util.Set;

/**
 * Handles the registration, un-registration and dispatching of commands.
 *
 * @param <S> command source
 */
public interface CommandHandler<S extends CommandSender> {

	/**
	 * Registers a new command.
	 *
	 * @param command command to register
	 * @return if the command was registered
	 */
	boolean registerCommand(RootSkriptCommandNode<S> command);

	/**
	 * Unregisters a command.
	 *
	 * @param command command to unregister
	 * @return if the command was unregistered
	 */
	boolean unregisterCommand(RootSkriptCommandNode<S> command);

	/**
	 * Returns command of given label or alias.
	 *
	 * @param label command label or alias
	 * @return command with given label if it exists
	 */
	@Nullable RootSkriptCommandNode<S> getCommand(String label);

	/**
	 * Returns all registered commands.
	 *
	 * @return all registered commands
	 */
	@UnmodifiableView Map<String, RootSkriptCommandNode<S>> getAllCommands();

	/**
	 * Dispatches a command from a given source.
	 * This method is responsible for parsing and executing the command logic.
	 *
	 * @param source source initiating the command
	 * @return true if the command dispatch was handled (not necessarily successful execution, but
	 *         the handler processed it), false otherwise
	 */
	boolean dispatchCommand(S source, String input);

	/**
	 * Returns supported command source types.
	 * <p>
	 * These types are used in the {@code executable by} entry in command structures.
	 *
	 * @return supported command source types
	 */
	@Unmodifiable Set<CommandSourceType> supportedTypes();

}

