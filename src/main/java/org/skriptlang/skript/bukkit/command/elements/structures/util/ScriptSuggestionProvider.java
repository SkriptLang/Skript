package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.Trigger;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;
import org.skriptlang.skript.bukkit.command.custom.ScriptArgumentType;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutor;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent.CommandSuggestion;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for managing {@link org.skriptlang.skript.bukkit.command.custom.ScriptBrigadierCommand} suggestions.
 * Similar to {@link com.mojang.brigadier.suggestion.SuggestionProvider}.
 */
@ApiStatus.Internal
public class ScriptSuggestionProvider {

	/**
	 * Modes for filtering suggestions based on context.
	 */
	public enum FilteringMode {

		/**
		 * No filtering is done to the provided suggestions.
		 * They are displayed as-is.
		 */
		NONE,

		/**
		 * Only suggestions starting with the current input will be displayed.
		 */
		STARTS_WITH,

		/**
		 * Only suggestions containing the current input will be displayed.
		 */
		CONTAINS

	}

	private final List<ArgumentData<?>> arguments;
	private final Trigger suggestionsProvider;

	/**
	 * @param suggestionsProvider A trigger to execute using {@link CommandSuggestionEvent}.
	 *  It is expected (though not required) that this trigger holds code to modify suggestions.
	 */
	public ScriptSuggestionProvider(List<ArgumentData<?>> arguments, Trigger suggestionsProvider) {
		this.arguments = arguments;
		this.suggestionsProvider = suggestionsProvider;
	}

	/**
	 * Obtains suggestions for a specific argument using this provider.
	 * @param argumentData Data about the argument suggestions are being obtained for.
	 * @param argument The underlying argument suggestions are being obtained for.
	 * @param context Context around the command execution.
	 * @param builder The builder to add suggestions to.
	 * @return Future to obtain suggestions.
	 */
	public CompletableFuture<Suggestions> getSuggestions(ArgumentData<?> argumentData, ArgumentType<?> argument,
														 CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		// build context
		List<ArgumentData<?>> currentArguments = arguments.subList(0, arguments.indexOf(argumentData));
		// TODO null executor is questionable, but currently only used by cooldowns which would not be available here
		Map<ArgumentData<?>, Object> mappedArguments;
		try {
			mappedArguments = ScriptCommandExecutor.getArguments(currentArguments, context,
				new ScriptCommandExecutionEvent(context.getNodes().getFirst().getNode().getName(), builder.getInput(),
					null, context.getSource()));
		} catch (CommandSyntaxException e) {
			// TODO is it better to just provide no arguments?
			return builder.buildFuture();
		}
		CommandSuggestionEvent suggestionEvent = new CommandSuggestionEvent(mappedArguments, argumentData,
			builder.getInput().substring(1), builder.getRemaining(), builder.getStart(), context.getSource());

		// obtain and suggest suggestions
		suggestionsProvider.execute(suggestionEvent);
		List<CommandSuggestion> suggestions = suggestionEvent.suggestions.get(argumentData.name());
		if (suggestions == null) { // nothing explicitly set, rely on argument's default suggestions
			if (argument instanceof ScriptArgumentType<?> scriptArgument) {
				return scriptArgument.listSuggestions(context, builder, suggestionEvent.filteringMode);
			}
			return argument.listSuggestions(context, builder);
		}
		for (CommandSuggestion suggestion : suggestions) {
			suggest(builder, suggestion, suggestionEvent.filteringMode);
		}
		return builder.buildFuture();
	}

	/**
	 * Attempts to add {@code suggestion} to {@code builder}.
	 * It expects that {@code suggestion} starts with (ignoring case) the currently entered input for the argument.
	 * @param builder The builder to add the suggestion to.
	 * @param suggestion The suggestion.
	 */
	public static void suggest(@NotNull SuggestionsBuilder builder, CommandSuggestion suggestion, FilteringMode filteringMode) {
		if (suggestion == null) {
			return;
		}

		if (filteringMode == FilteringMode.NONE) {
			Component tooltip = suggestion.tooltip();
			if (tooltip == null) {
				builder.suggest(suggestion.suggestion());
			} else {
				builder.suggest(suggestion.suggestion(), MessageComponentSerializer.message().serialize(tooltip));
			}
			return;
		}

		// treat <foo b> as a valid match for <foo_bar>
		// treat <"foo b> as a valid match for <foo_bar>
		String remaining = builder.getRemainingLowerCase();
		if (!remaining.isEmpty() && remaining.charAt(0) == '"') {
			remaining = remaining.substring(1);
		}
		if (remaining.contains(" ")) {
			remaining = remaining.replace(' ', '_');
		}
		String suggestionLower = suggestion.suggestion().toLowerCase(Locale.ENGLISH);
		if (!suggestionLower.isEmpty() && suggestionLower.charAt(0) == '"') {
			suggestionLower = suggestionLower.substring(1);
		}
		if (suggestionLower.contains(" ")) {
			suggestionLower = suggestionLower.replace(' ', '_');
		}

		if ((filteringMode == FilteringMode.STARTS_WITH && !suggestionLower.startsWith(remaining)) ||
			(filteringMode == FilteringMode.CONTAINS && !suggestionLower.contains(remaining))) {
			return;
		}

		Component tooltip = suggestion.tooltip();
		if (tooltip == null) {
			builder.suggest(suggestion.suggestion());
		} else {
			builder.suggest(suggestion.suggestion(), MessageComponentSerializer.message().serialize(tooltip));
		}
	}

}
