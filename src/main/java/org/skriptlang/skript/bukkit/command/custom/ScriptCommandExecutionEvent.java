package org.skriptlang.skript.bukkit.command.custom;

import io.papermc.paper.command.brigadier.CommandSourceStack;

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

	public ScriptCommandExecutionEvent(String label, String rawInput, ScriptCommandExecutor commandExecutor, CommandSourceStack source) {
		super(source);
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

}
