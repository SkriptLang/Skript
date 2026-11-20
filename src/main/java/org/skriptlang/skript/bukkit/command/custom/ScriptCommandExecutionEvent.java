package org.skriptlang.skript.bukkit.command.custom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Event for executing {@link org.skriptlang.skript.bukkit.command.elements.structures.StructCommand} commands with.
 */
public class ScriptCommandExecutionEvent extends ScriptCommandEvent {

	private final String label;
	private final String rawInput;
	private final ScriptCommandExecutor commandExecutor;

	final Map<String, Object> arguments = new HashMap<>();
	int result = Command.SINGLE_SUCCESS;
	@Nullable Component failureMessage;

	public ScriptCommandExecutionEvent(String label, String rawInput, ScriptCommandExecutor commandExecutor,
		CommandContext<CommandSourceStack> context) {
		super(context);
		this.label = label;
		this.rawInput = rawInput;
		this.commandExecutor = commandExecutor;
	}

	/**
	 * @return The label of the executed command.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return The full raw input being executed.
	 */
	public String getRawInput() {
		return rawInput;
	}

	/**
	 * @return The command executor being used to execute this command (perform logic).
	 */
	public ScriptCommandExecutor getCommandExecutor() {
		return commandExecutor;
	}

	/**
	 * @return A map of all available arguments and their values.
	 */
	public Map<String, Object> getArguments() {
		return Collections.unmodifiableMap(arguments);
	}

	/**
	 * Obtains an argument by its name.
	 * @param name The name of the argument.
	 * @return The value of the argument.
	 */
	public Object getArgument(String name) {
		return arguments.get(name);
	}

	/**
	 * @return The result for this command execution.
	 */
	public int getResult() {
		return result;
	}

	/**
	 * Sets the result for this command execution.
	 * @param result The new result value.
	 */
	public void setResult(int result) {
		this.result = result;
	}

	/**
	 * @return A component describing why this command execution failed.
	 * Null if this command execution did not fail.
	 */
	public @Nullable Component getFailureMessage() {
		return failureMessage;
	}

	/**
	 * Sets the failure message for this command execution.
	 * @param failureMessage A component describing why this command execution failed.
	 *  {@code null} indicates that command execution should not fail.
	 */
	public void setFailureMessage(@Nullable Component failureMessage) {
		this.failureMessage = failureMessage;
	}

}
