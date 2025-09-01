package org.skriptlang.skript.brigadier;

import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.MessageComponent;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.skriptlang.skript.lang.command.SkriptCommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link SuggestionProvider} that accepts returning Skript MessageComponents
 * instead of Brigadier Messages.
 * <p>
 * Tooltips of those MessageComponents are serialized as JSON and used as tooltips of Brigadier Messages.
 *
 * @param <S> command source
 */
@FunctionalInterface
public interface SkriptSuggestionProvider<S extends SkriptCommandSender> extends SuggestionProvider<S> {

	@Override
	default CompletableFuture<Suggestions> getSuggestions(CommandContext<S> commandContext,
			SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
		return getSuggestions(commandContext)
			.thenApply(components -> {
				for (MessageComponent component : components) {
					Message tooltip = null;

					if (component.hoverEvent != null
						&& component.hoverEvent.action == MessageComponent.HoverEvent.Action.show_text) {
						//noinspection deprecation
						BaseComponent[] baseComponents = BungeeConverter
							.convert(ChatMessages.parseToArray(component.hoverEvent.value));
						Component adventure = BungeeComponentSerializer.get().deserialize(baseComponents);
						tooltip = () -> JSONComponentSerializer.json().serialize(adventure);
					}
					// skip any blank components
					if (component.text.isBlank())
						continue;
					suggestionsBuilder.suggest(component.text, tooltip);
				}
				return suggestionsBuilder.build();
			});
	}

	/**
	 * Returns suggestions for given context as Skript MessageComponents.
	 * <p>
	 * Tooltips of those MessageComponents are mapped to tooltips of the suggestions if
	 * they are provided as text.
	 *
	 * @param commandContext command context
	 * @return suggestions
	 */
	CompletableFuture<List<MessageComponent>> getSuggestions(CommandContext<S> commandContext)
		throws CommandSyntaxException;

}
