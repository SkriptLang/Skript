package org.skriptlang.skript.lang.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandUsage;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class CommandUtils {

	/**
	 * Table mapping entry container and entry keys (suggestions and trigger) to loaded triggers.
	 * <p>
	 * This is to ensure each trigger is loaded only once for command arguments composed of multiple
	 * command nodes.
	 *
	 * @see CommandArgument
	 */
	private static final ThreadLocal<Table<EntryContainer, String, Trigger>> LOADED_TRIGGERS
		= ThreadLocal.withInitial(HashBasedTable::create);

	private CommandUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Clears all loaded triggers.
	 * <p>
	 * This should be called after command nodes are created to
	 * prevent storing further unused triggers.
	 */
	static void clearLoadedTriggers() {
		LOADED_TRIGGERS.get().clear();
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
	 * Creates a command nodes from a sub command entry data.
	 * <p>
	 * If parent is provided, the created nodes are added as its children.
	 * Parent properties are also copied to the created nodes.
	 *
	 * @param handler command handler
	 * @param subCommandData sub command entry data
	 * @param parent parent command node (inherits command properties), this is always present for
	 *  sub command entries (either another sub command entry or the root command)
	 * @return created skript command nodes or empty list if the creation failed
	 */
	public static List<SkriptCommandNode<SkriptCommandSender>> createCommandNode(
			CommandHandler<SkriptCommandSender> handler, SubCommandEntryData.Parsed subCommandData,
			SkriptCommandNode.Builder<SkriptCommandSender, ?> parent) {
		Preconditions.checkNotNull(parent, "Sub-command entry data always have a parent command node");
		if (subCommandData.entryContainer() == null || subCommandData.arguments() == null)
			return Collections.emptyList();
		return createCommandNode(handler, subCommandData.entryContainer(), parent, subCommandData.arguments());
	}

	/**
	 * Creates a command nodes from an entry container and parsed command arguments.
	 * <p>
	 * If parent is provided, the created nodes are added as its children.
	 * Parent properties are also copied to the created nodes.
	 *
	 * @param handler command handler
	 * @param entryContainer entry container
	 * @param parent parent command node (inherits command properties)
	 * @param arguments arguments
	 * @return created skript command nodes or empty list if the creation failed
	 */
	public static List<SkriptCommandNode<SkriptCommandSender>> createCommandNode(
			CommandHandler<SkriptCommandSender> handler, EntryContainer entryContainer,
			@Nullable SkriptCommandNode.Builder<SkriptCommandSender, ?> parent, List<CommandArgument> arguments) {
		if (arguments.isEmpty()) {
			Skript.error("Command nodes can not be empty; arguments are missing");
			return Collections.emptyList();
		}

		// list of builder groups (lists), each representing nodes of one parsed command argument
		List<List<? extends SkriptCommandNode.Builder<SkriptCommandSender, ?>>> builderGroups = new LinkedList<>();

		for (int i = 0; i < arguments.size(); i++) {
			var builders = arguments.get(i).emptyBuilders();
			for (SkriptCommandNode.Builder<SkriptCommandSender, ?> builder : builders) {
				// first we copy parent properties
				copyParentProperties(builder, parent);
				// now we override them if they are present in the entry container for this node
				if (!setCommandNodeProperties(handler, entryContainer, builder, i == arguments.size() - 1))
					return Collections.emptyList();
			}
			builderGroups.add(builders);
		}

		// now we prepared all builders, it is time to connect them together, from last to first one
		for (int i = builderGroups.size() - 1; i > 0; i--) {
			for (SkriptCommandNode.Builder<SkriptCommandSender, ?> previous : builderGroups.get(i - 1)) {
				builderGroups.get(i).forEach(previous::then);
			}
		}

		var firstNodes = builderGroups.get(0).stream().map(SkriptCommandNode.Builder::build).toList();
		if (parent != null)
			firstNodes.forEach(parent::then);
		return firstNodes;
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

			Trigger trigger = LOADED_TRIGGERS.get().row(entryContainer).computeIfAbsent(
				SubCommandEntryData.SUGGESTIONS_KEY, key -> {
					ParserInstance parser = ParserInstance.get();
					ParserInstance.Backup parserBackup = parser.backup();
					parser.reset();
					parser.setCurrentEvent("command suggestions", BrigadierSuggestionsEvent.class);
					Trigger loaded = suggestionsReturnHandler.loadReturnableTrigger(suggestionsNode,
						"command suggestions", new SimpleEvent());
					loaded.setLineNumber(suggestionsNode.getLine());
					parser.restoreBackup(parserBackup);
					return loaded;
				});

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
			Trigger trigger = LOADED_TRIGGERS.get().row(entryContainer).computeIfAbsent(
				SubCommandEntryData.TRIGGER_KEY, key -> {
					SectionNode triggerNode = entryContainer.get(SubCommandEntryData.TRIGGER_KEY, SectionNode.class,
						false);
					ParserInstance parser = ParserInstance.get();
					ParserInstance.Backup parserBackup = parser.backup();
					parser.reset();
					parser.setCurrentEvent("command trigger", BrigadierCommandEvent.class);
					Trigger loaded = new Trigger(ParserInstance.get().getCurrentScript(),
						"command trigger", new SimpleEvent(), ScriptLoader.loadItems(triggerNode));
					loaded.setLineNumber(triggerNode.getLine());
					parser.restoreBackup(parserBackup);
					return loaded;
				});
			builder.executes(ctx -> runTriggerOnMainThread(trigger, new BrigadierCommandEvent(ctx)) ? 1 : 0);
		}

		if (entryContainer.hasEntry(SubCommandEntryData.SUBCOMMAND_KEY)) {
			List<SubCommandEntryData.Parsed> subCommands = entryContainer.getAll(SubCommandEntryData.SUBCOMMAND_KEY,
				SubCommandEntryData.Parsed.class, false);
			for (SubCommandEntryData.Parsed subCommand : subCommands) {
				if (createCommandNode(handler, subCommand, builder).isEmpty())
					return false;
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
