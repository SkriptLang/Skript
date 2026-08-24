package org.skriptlang.skript.bukkit.command.elements.structures.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.event.server.TabCompleteEvent;
import org.skriptlang.skript.bukkit.command.custom.ScriptArgumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Internal suggestion provider for preserving support for suggestions provided through the {@link TabCompleteEvent}.
 */
final class LegacyTabCompleteEventSuggestionProvider {

	/**
	 * @param arguments Arguments to modify.
	 * @return List of modified arguments. Note that arguments are not guaranteed to be modified.
	 */
	public static List<ScriptArgumentBuilder> addMissingSuggestionProviders(List<ScriptArgumentBuilder> arguments) {
		return arguments.stream()
			.map(builder -> {
				CommandNode<CommandSourceStack> node = builder.builder().build();
				CommandNode<CommandSourceStack> newNode = modifySuggestions(node);
				if (newNode == node) { // no modifications necessary
					return builder;
				}
				ArgumentBuilder<CommandSourceStack, ?> newNodeBuilder = newNode.createBuilder();
				newNode.getChildren().forEach(newNodeBuilder::then);
				return new ScriptArgumentBuilder(newNodeBuilder, builder.data());
			})
			.toList();
	}

	/**
	 * This method traverses the command node tree.
	 * At any level, if there is at least one argument node, and none provide suggestions,
	 *  we insert a {@link LegacyTabCompleteEventSuggestionProvider} on the first encountered argument node.
	 * @return The modified node, or {@code node} if no modifications were necessary.
	 */
	private static CommandNode<CommandSourceStack> modifySuggestions(CommandNode<CommandSourceStack> node) {
		if (node.getChildren().isEmpty()) {
			return node;
		}

		List<CommandNode<CommandSourceStack>> newChildren = new ArrayList<>();
		boolean hasNewChildren = false;
		int firstArgumentIndex = -1;
		for (CommandNode<CommandSourceStack> child : node.getChildren()) {
			CommandNode<CommandSourceStack> modifiedChild = modifySuggestions(child);
			if (modifiedChild != child) {
				child = modifiedChild;
				hasNewChildren = true;
			}
			newChildren.add(child);
			if (firstArgumentIndex == -1 && child instanceof ArgumentCommandNode<?,?>) {
				firstArgumentIndex = newChildren.size() - 1;
			}
		}

		if (hasNewChildren || firstArgumentIndex >= 0) {
			ArgumentBuilder<CommandSourceStack, ?> builder = node.createBuilder();
			if (firstArgumentIndex >= 0) {
				CommandNode<CommandSourceStack> existingChild = newChildren.get(firstArgumentIndex);
				//noinspection unchecked
				RequiredArgumentBuilder<CommandSourceStack, ?> childBuilder =
					(RequiredArgumentBuilder<CommandSourceStack, ?>) existingChild.createBuilder();
				existingChild.getChildren().forEach(childBuilder::then);
				var suggestionProvider = new LegacyTabCompleteEventSuggestionProvider(childBuilder.getType());
				childBuilder.suggests(suggestionProvider::getSuggestions);
				newChildren.set(firstArgumentIndex, childBuilder.build());
			}
			newChildren.forEach(builder::then);
			return builder.build();
		}

		return node;
	}

	private final ArgumentType<?> argument;

	private LegacyTabCompleteEventSuggestionProvider(ArgumentType<?> argument) {
		this.argument = argument;
	}

	private CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
	                                                             SuggestionsBuilder builder) {
		TabCompleteEvent tabCompleteEvent = new TabCompleteEvent(context.getSource().getSender(),
			builder.getInput(), new ArrayList<>(), true, context.getSource().getLocation());
		if (tabCompleteEvent.callEvent()) {
			if (tabCompleteEvent.getCompletions().isEmpty()) {
				if (argument instanceof CustomArgumentType<?, ?> customArgument &&
					!(argument instanceof ScriptArgumentType.Suggesting<?>)) {
					// for custom arguments that aren't our suggesting arguments, fallback to the native type
					return customArgument.getNativeType().listSuggestions(context, builder);
				}
				return argument.listSuggestions(context, builder);
			} else {
				tabCompleteEvent.getCompletions().forEach(builder::suggest);
			}
		}
		return builder.buildFuture();
	}

}
