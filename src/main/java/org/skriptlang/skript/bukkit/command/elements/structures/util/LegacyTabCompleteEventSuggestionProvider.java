package org.skriptlang.skript.bukkit.command.elements.structures.util;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Internal suggestion provider for preserving support for suggestions provided through the {@link TabCompleteEvent}.
 */
final class LegacyTabCompleteEventSuggestionProvider {

	private LegacyTabCompleteEventSuggestionProvider() { }

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
				ArgumentBuilder<CommandSourceStack, ?> childBuilder = existingChild.createBuilder();
				existingChild.getChildren().forEach(childBuilder::then);
				//noinspection unchecked
				((RequiredArgumentBuilder<CommandSourceStack, ?>) childBuilder)
					.suggests(LegacyTabCompleteEventSuggestionProvider::getSuggestions);
				newChildren.set(firstArgumentIndex, childBuilder.build());
			}
			newChildren.forEach(builder::then);
			return builder.build();
		}

		return node;
	}

	private static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
	                                                             SuggestionsBuilder builder) {
		TabCompleteEvent tabCompleteEvent = new TabCompleteEvent(context.getSource().getSender(),
			builder.getInput(), new ArrayList<>(), true, context.getSource().getLocation());
		if (tabCompleteEvent.callEvent()) {
			tabCompleteEvent.getCompletions().forEach(builder::suggest);
		}
		return builder.buildFuture();
	}

}
