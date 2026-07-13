package org.skriptlang.skript.bukkit.command.elements.structures.util;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.ScriptSuggestionProvider.FilteringMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal event for managing context during "suggestions" entry evaluation in {@link SubCommandEntryData}.
 */
@ApiStatus.Internal
public class CommandSuggestionEvent extends ScriptCommandEvent {

	/**
	 * @param suggestion The suggestion.
	 * @param tooltip An optional tooltip to display when hovering over this suggestion.
	 */
	public record CommandSuggestion(String suggestion, @Nullable Component tooltip) {

		public CommandSuggestion(String suggestion) {
			this(suggestion, null);
		}

	}

	private final Map<ArgumentData<?>, Object> previousArguments;
	private final ArgumentData<?> currentArgument;
	private final String fullInput;
	private final String input;
	private final int inputStartIndex;

	/**
	 * Map keyed by argument name.
	 */
	public final Map<String, List<CommandSuggestion>> suggestions = new HashMap<>();
	public FilteringMode filteringMode = FilteringMode.STARTS_WITH;

	public CommandSuggestionEvent(Map<ArgumentData<?>, Object> previousArguments, ArgumentData<?> currentArgument,
	                              String fullInput, String input, int inputStartIndex, CommandSourceStack source) {
		super(source);
		this.previousArguments = previousArguments;
		this.currentArgument = currentArgument;
		this.fullInput = fullInput;
		this.input = input;
		this.inputStartIndex = inputStartIndex;
	}

	/**
	 * @return The arguments that have been parsed so far.
	 */
	public Map<ArgumentData<?>, Object> getPreviousArguments() {
		return previousArguments;
	}

	/**
	 * @return The argument currently being suggested for.
	 */
	public ArgumentData<?> getCurrentArgument() {
		return currentArgument;
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

}
