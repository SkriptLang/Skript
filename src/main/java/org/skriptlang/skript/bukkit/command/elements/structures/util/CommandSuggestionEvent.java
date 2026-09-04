package org.skriptlang.skript.bukkit.command.elements.structures.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandEvent;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutor;
import org.skriptlang.skript.bukkit.command.elements.structures.util.ScriptSuggestionProvider.FilteringMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private final CommandContext<CommandSourceStack> context;
	private final ScriptCommandExecutionEvent argumentEventContext;
	private final Set<String> providedArguments;
	private final List<ArgumentData<?>> previousArguments;
	private final Map<ArgumentData<?>, Object> previousArgumentValues = new HashMap<>();
	private final ArgumentData<?> currentArgument;
	private final String fullInput;
	private final String input;
	private final int inputStartIndex;

	/**
	 * Map keyed by argument name.
	 */
	public final Map<String, List<CommandSuggestion>> suggestions = new HashMap<>();
	public FilteringMode filteringMode = FilteringMode.STARTS_WITH;

	public CommandSuggestionEvent(CommandContext<CommandSourceStack> context, List<ArgumentData<?>> previousArguments,
								  ArgumentData<?> currentArgument, String fullInput, String input, int inputStartIndex) {
		super(context);
		this.context = context;
		// IMPORTANT: null executor is questionable, but currently only used by cooldowns which would not be available here
		this.argumentEventContext = new ScriptCommandExecutionEvent(context.getNodes().getFirst().getNode().getName(),
			'/' + fullInput, null, context);
		this.providedArguments = ScriptCommandExecutor.getProvidedArguments(context);
		this.previousArguments = previousArguments;
		this.currentArgument = currentArgument;
		this.fullInput = fullInput;
		this.input = input;
		this.inputStartIndex = inputStartIndex;
	}

	/**
	 * Obtains the value of an argument.
	 * @param argument The argument to obtain the value of.
	 * @return The value associated with {@code argument}.
	 */
	public Object getPreviousArgument(ArgumentData<?> argument) {
		if (previousArguments.contains(argument)) {
			if (previousArgumentValues.containsKey(argument)) {
				return previousArgumentValues.get(argument);
			}
			Object value;
			try {
				value = ScriptCommandExecutor.getArgument(argument, providedArguments, context, argumentEventContext);
			} catch (CommandSyntaxException ignored) {
				value = null;
			}
			// cache resolved argument for future usages
			previousArgumentValues.put(argument, value);
			return value;
		}
		return null;
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
