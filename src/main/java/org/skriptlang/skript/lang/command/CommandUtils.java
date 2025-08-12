package org.skriptlang.skript.lang.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandUsage;
import org.skriptlang.skript.brigadier.SkriptSuggestionProvider;
import org.skriptlang.skript.bukkit.command.BrigadierCommandEvent;
import org.skriptlang.skript.bukkit.command.BrigadierSuggestionsEvent;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.chat.ChatMessages;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.brigadier.ArgumentSkriptCommandNode;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;
import org.skriptlang.skript.brigadier.SkriptCommandNode;
import org.skriptlang.skript.lang.entry.EntryContainer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class CommandUtils {

	private CommandUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks whether the entry container can represent a command entry.
	 *
	 * @param entryContainer entry container
	 * @return whether the given container can represent a command entry
	 */
	public static boolean isValidCommandNode(EntryContainer entryContainer) {
		if (!entryContainer.hasEntry(SubCommandEntryData.SUBCOMMAND_KEY)
			&& !entryContainer.hasEntry(SubCommandEntryData.TRIGGER_KEY)) {
			Skript.error("Command node needs to have either a trigger or another sub-command entry.");
			return false;
		}
		return true;
	}

	/**
	 * Creates a command node from a sub command entry data.
	 * <p>
	 * If parent is provided, the created node is added as its child.
	 * Parent properties are also copied to the created node.
	 *
	 * @param handler command handler
	 * @param subCommandData sub command entry data
	 * @param parent parent command node (inherits command properties), this is always present for
	 *  sub command entries (either another sub command entry or the root command)
	 * @return created skript command node or null if the creation failed
	 */
	public static @Nullable SkriptCommandNode<SkriptCommandSender> createCommandNode(CommandHandler<SkriptCommandSender> handler,
			SubCommandEntryData.Parsed subCommandData, SkriptCommandNode.Builder<SkriptCommandSender, ?> parent) {
		Preconditions.checkNotNull(parent, "Sub-command entry data always have a parent command node");
		if (subCommandData.entryContainer() == null || subCommandData.arguments() == null)
			return null;
		return createCommandNode(handler, subCommandData.entryContainer(), parent, subCommandData.arguments());
	}

	/**
	 * Creates a command node from an entry container and parsed command arguments.
	 * <p>
	 * If parent is provided, the created node is added as its child.
	 * Parent properties are also copied to the created node.
	 *
	 * @param handler command handler
	 * @param entryContainer entry container
	 * @param parent parent command node (inherits command properties)
	 * @param arguments arguments
	 * @return created skript command node or null if the creation failed
	 */
	public static @Nullable SkriptCommandNode<SkriptCommandSender> createCommandNode(CommandHandler<SkriptCommandSender> handler,
			EntryContainer entryContainer, @Nullable SkriptCommandNode.Builder<SkriptCommandSender, ?> parent,
			List<CommandArgument> arguments) {
		if (arguments.isEmpty()) {
			Skript.error("Command nodes can not be empty; arguments are missing");
			return null;
		}

		List<SkriptCommandNode.Builder<SkriptCommandSender, ?>> builders = new LinkedList<>();
		for (int i = 0; i < arguments.size(); i++) {
			var builder = arguments.get(i).emptyBuilder();
			// first we copy parent properties
			copyParentProperties(builder, parent);
			// now we override them if they are present in the entry container for this node
			if (!setCommandNodeProperties(handler, entryContainer, builder, i == arguments.size() - 1))
				return null;
			builders.add(builder);
		}

		// now we prepared all builders, it is time to connect them together, from last to first one
		for (int i = builders.size() - 1; i > 0; i--) {
			var current = builders.get(i);
			var previous = builders.get(i - 1);
			previous.then(current);
		}

		SkriptCommandNode<SkriptCommandSender> firstNode = builders.get(0).build();
		if (parent != null)
			parent.then(firstNode);
		return firstNode;
	}

	private static void copyParentProperties(SkriptCommandNode.Builder<SkriptCommandSender, ?> builder,
			@Nullable SkriptCommandNode.Builder<SkriptCommandSender, ?> parent) {
		if (parent == null) return;
		builder.permission(parent.getPermission());
		builder.permissionMessage(parent.getPermissionMessage());
		builder.possibleSources(parent.getPossibleSources());
		builder.cooldown(parent.getCooldown());
	}

	/**
	 * Updates the command node builder with command properties of given entry container.
	 *
	 * @param handler command handler
	 * @param entryContainer entry container
	 * @param builder builder
	 * @param last whether the node is at the end of the command argument
	 *  definition, e.g. for this command '/foo bar world', 'world' is the last node
	 * @return false if the provided data are not compatible with given handler, else true
	 */
	public static boolean setCommandNodeProperties(CommandHandler<SkriptCommandSender> handler,
			EntryContainer entryContainer, SkriptCommandNode.Builder<SkriptCommandSender, ?> builder,
			boolean last) {

		// root command properties
		if (builder instanceof RootSkriptCommandNode.Builder<?> rootBuilder) {
			if (entryContainer.hasEntry(StructGeneralCommand.DESCRIPTION_KEY)) {
				rootBuilder.description(entryContainer.get(StructGeneralCommand.DESCRIPTION_KEY, String.class,
					false));
			}
			if (entryContainer.hasEntry(StructGeneralCommand.USAGE_KEY)) {
				VariableString usage = entryContainer.getOptional(StructGeneralCommand.USAGE_KEY, VariableString.class,
					false);
				// brigadier commands get too complex to create meaningful usage, default to '/literal'
				rootBuilder.usage(new CommandUsage(usage, "/" + rootBuilder.getLiteral()));
			}
			if (entryContainer.hasEntry(StructGeneralCommand.ALIASES_KEY)) {
				//noinspection unchecked
				rootBuilder.aliases(entryContainer.get(StructGeneralCommand.ALIASES_KEY, List.class,
					true));
			}
		}

		if (entryContainer.hasEntry(SubCommandEntryData.PERMISSION_KEY)) {
			builder.permission(entryContainer.getOptional(SubCommandEntryData.PERMISSION_KEY,
				String.class, false));
		}
		if (entryContainer.hasEntry(SubCommandEntryData.PERMISSION_MSG_KEY)) {
			builder.permissionMessage(entryContainer.getOptional(SubCommandEntryData.PERMISSION_MSG_KEY,
				VariableString.class, false));
		}

		if (entryContainer.hasEntry(SubCommandEntryData.EXECUTABLE_KEY)) {
			List<CommandSourceType> possibleSources = new LinkedList<>();
			for (Object source : entryContainer.get(SubCommandEntryData.EXECUTABLE_KEY, List.class, true)) {
				var found = handler.supportedTypes().stream()
					.filter(type -> type.names().contains(source.toString()))
					.findFirst();
				if (found.isPresent()) {
					possibleSources.add(found.get());
					continue;
				}
				Skript.error("Invalid command source type: " + source);
				return false;
			}
			builder.possibleSources(possibleSources);
		}

		// TODO cooldown

		// suggestions, trigger and children are only considered for the last node
		if (!last) return true;

		if (entryContainer.hasEntry(SubCommandEntryData.SUGGESTIONS_KEY)) {
			if (!(builder instanceof ArgumentSkriptCommandNode.Builder<?,?>)) {
				Skript.error("Custom suggestions can only be provided for argument nodes");
				return false;
			}
			SectionNode suggestionsNode = entryContainer.get(SubCommandEntryData.SUGGESTIONS_KEY,
				SectionNode.class, false);

			ReturnHandler<String> suggestionsReturnHandler = new ReturnHandler<>() {
				@Override
				public void returnValues(Event event, Expression<? extends String> value) {
					if (!(event instanceof BrigadierSuggestionsEvent bse)) return;
					bse.setSuggestions(value.getAll(event));
				}

				@Override
				public boolean isSingleReturnValue() {
					return false;
				}

				@Override
				public @NotNull Class<? extends String> returnValueType() {
					return String.class;
				}
			};

			ParserInstance parser = ParserInstance.get();
			ParserInstance.Backup parserBackup = parser.backup();
			parser.reset();
			parser.setCurrentEvent("command suggestions", BrigadierSuggestionsEvent.class);
			Trigger trigger = suggestionsReturnHandler.loadReturnableTrigger(suggestionsNode,
				"command suggestions", new SimpleEvent());
			trigger.setLineNumber(suggestionsNode.getLine());
			parser.restoreBackup(parserBackup);

			SkriptSuggestionProvider<SkriptCommandSender> suggestionProvider = ctx -> {
				BrigadierSuggestionsEvent event = new BrigadierSuggestionsEvent(ctx);
				runTriggerOnMainThread(trigger, event);
				// the chat message parsing can be done async
				return CompletableFuture.supplyAsync(() -> Arrays.stream(event.getSuggestions())
					.flatMap(s -> ChatMessages.parse(s).stream())
					.toList());
			};

			//noinspection unchecked
			((ArgumentSkriptCommandNode.Builder<SkriptCommandSender, ?>) builder).suggests(suggestionProvider);
		}

		if (entryContainer.hasEntry(SubCommandEntryData.TRIGGER_KEY)) {
			SectionNode triggerNode = entryContainer.get(SubCommandEntryData.TRIGGER_KEY, SectionNode.class,
				false);
			ParserInstance parser = ParserInstance.get();
			ParserInstance.Backup parserBackup = parser.backup();
			parser.reset();
			parser.setCurrentEvent("command trigger", BrigadierCommandEvent.class);
			Trigger trigger = new Trigger(ParserInstance.get().getCurrentScript(),
				"command trigger", new SimpleEvent(), ScriptLoader.loadItems(triggerNode));
			trigger.setLineNumber(triggerNode.getLine());
			parser.restoreBackup(parserBackup);
			builder.executes(ctx -> runTriggerOnMainThread(trigger, new BrigadierCommandEvent(ctx)) ? 1 : 0);
		}

		if (entryContainer.hasEntry(SubCommandEntryData.SUBCOMMAND_KEY)) {
			List<SubCommandEntryData.Parsed> subCommands = entryContainer.getAll(SubCommandEntryData.SUBCOMMAND_KEY,
				SubCommandEntryData.Parsed.class, false);
			for (SubCommandEntryData.Parsed subCommand : subCommands) {
				createCommandNode(handler, subCommand, builder);
			}
		}

		return true;
	}

	private static boolean runTriggerOnMainThread(Trigger trigger, Event event) {
		if (Bukkit.isPrimaryThread()) {
			return trigger.execute(event);
		} else {
			Executor executor = Bukkit.getScheduler().getMainThreadExecutor(Skript.getInstance());
			try {
				return CompletableFuture.supplyAsync(() -> trigger.execute(event), executor).get();
			} catch (Exception exception) {
				return false;
			}
		}
	}

}
