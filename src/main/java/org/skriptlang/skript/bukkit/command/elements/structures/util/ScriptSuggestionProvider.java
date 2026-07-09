package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.Trigger;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for managing {@link org.skriptlang.skript.bukkit.command.custom.ScriptBrigadierCommand} suggestions.
 * Similar to {@link com.mojang.brigadier.suggestion.SuggestionProvider}.
 */
@ApiStatus.Internal
public class ScriptSuggestionProvider {

	private final Trigger suggestionsProvider;

	/**
	 * @param suggestionsProvider A trigger to execute using {@link CommandSuggestionEvent}.
	 *  It is expected (though not required) that this trigger holds code to modify suggestions.
	 */
	public ScriptSuggestionProvider(Trigger suggestionsProvider) {
		this.suggestionsProvider = suggestionsProvider;
	}

	/**
	 * Obtains suggestions for a specific argument using this provider.
	 * @param argumentName The name of the argument suggestions are being obtained for.
	 * @param context Context around the command execution.
	 * @param builder The builder to add suggestions to
	 * @param argument The underlying argument suggestions are being obtained for.
	 * @return Future to obtain suggestions.
	 */
	public CompletableFuture<Suggestions> getSuggestions(String argumentName, CommandContext<CommandSourceStack> context,
	                                                     SuggestionsBuilder builder, ArgumentType<?> argument) {
		CommandSuggestionEvent suggestionEvent = new CommandSuggestionEvent(builder.getInput(), builder.getRemaining(), builder.getStart());
		suggestionsProvider.execute(suggestionEvent);
		List<String> suggestions = suggestionEvent.suggestions.get(argumentName);
		if (suggestions == null) { // nothing explicitly set, rely on argument's default suggestions
			return argument.listSuggestions(context, builder);
		}
		for (String suggestion : suggestions) {
			suggest(builder, suggestion);
		}
		return builder.buildFuture();
	}

	/**
	 * Attempts to add {@code suggestion} to {@code builder}.
	 * It expects that {@code suggestion} starts with (ignoring case) the currently entered input for the argument.
	 * @param builder The builder to add the suggestion to.
	 * @param suggestion The suggestion.
	 */
	public static void suggest(@NotNull SuggestionsBuilder builder, String suggestion) {
		if (suggestion == null) {
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
		if (suggestion.toLowerCase(Locale.ENGLISH).startsWith(remaining)) {
			builder.suggest(suggestion);
		}
	}

}
