package org.skriptlang.skript.bukkit.command.elements.structures.util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal event for managing context during "suggestions" entry evaluation in {@link SubCommandEntryData}.
 */
@ApiStatus.Internal
public class CommandSuggestionEvent extends Event {

	private final Map<ArgumentData<?>, Object> currentArguments;
	private final String fullInput;
	private final String input;
	private final int inputStartIndex;

	/**
	 * Map keyed by argument name.
	 */
	public final Map<String, List<String>> suggestions = new HashMap<>();

	public CommandSuggestionEvent(Map<ArgumentData<?>, Object> currentArguments, String fullInput, String input, int inputStartIndex) {
		this.currentArguments = currentArguments;
		this.fullInput = fullInput;
		this.input = input;
		this.inputStartIndex = inputStartIndex;
	}

	/**
	 * @return The arguments that have been parsed so far.
	 */
	public Map<ArgumentData<?>, Object> getCurrentArguments() {
		return currentArguments;
	}

	/**
	 * @return The full input entered so far.
	 */
	public String getFullInput() {
		return fullInput;
	}

	/**
	 * @return The input representing the current argument (the one being suggested for).
	 */
	public String getInput() {
		return input;
	}

	/**
	 * @return Index representing the start of {@link #getInput()} in {@link #getFullInput()}.
	 */
	public int getInputStartIndex() {
		return inputStartIndex;
	}

	@Override
	@Contract("-> fail")
	public @NotNull HandlerList getHandlers() {
		throw new UnsupportedOperationException();
	}

}
