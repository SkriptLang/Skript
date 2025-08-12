package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.Skript;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;
import org.skriptlang.skript.lang.command.CommandHandler;
import org.skriptlang.skript.lang.command.CommandSourceType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Command Handler implementation for Paper environment.
 */
// TODO thread safety
public class PaperCommandHandler implements CommandHandler<BukkitCommandSender>, Listener {

	private final Map<String, RootSkriptCommandNode<BukkitCommandSender>> commands = new ConcurrentHashMap<>();
	private @Nullable CommandDispatcher<BukkitCommandSender> dispatcher;

	/**
	 * Ensures the dispatcher instance is available.
	 */
	private void ensureDispatcher() {
		if (dispatcher != null) return;
		dispatcher = new CommandDispatcher<>();
		commands.values().forEach(cmd -> dispatcher.register(cmd.flatToBuilder()));
	}

	@Override
	public boolean registerCommand(RootSkriptCommandNode<BukkitCommandSender> command) {
		if (commands.containsKey(command.getLiteral())) {
			Skript.error("Command " + command.getLiteral() + " is already registered");
			return false;
		}
		commands.put(command.getLiteral(), command);
		// TODO proper registration with aliases and help page
		Bukkit.getCommandMap().getKnownCommands().put(command.getLiteral(), new WrappedBrigadierCommand(command));
		dispatcher = null; // reset dispatcher
		if (Skript.getInstance().isEnabled())
			Bukkit.getScheduler().runTaskLater(Skript.getInstance(), PaperCommandUtils::syncCommands, 1);
		return true;
	}

	@Override
	public boolean unregisterCommand(RootSkriptCommandNode<BukkitCommandSender> command) {
		boolean result = commands.remove(command.getLiteral(), command);
		if (!result) return false;
		Bukkit.getCommandMap().getKnownCommands().remove(command.getLiteral());
		dispatcher = null; // reset dispatcher
		if (Skript.getInstance().isEnabled())
			Bukkit.getScheduler().runTaskLater(Skript.getInstance(), PaperCommandUtils::syncCommands, 1);
		return true;
	}

	@Override
	public @Nullable RootSkriptCommandNode<BukkitCommandSender> getCommand(String label) {
		return commands.get(label);
	}

	@Override
	public @UnmodifiableView Map<String, RootSkriptCommandNode<BukkitCommandSender>> getAllCommands() {
		return Collections.unmodifiableMap(commands);
	}

	@Override
	public boolean dispatchCommand(BukkitCommandSender source, String input) {
		ensureDispatcher();
		assert dispatcher != null;
		// TODO
		//  if the command does not exist return false
		//  send error message to source similar to vanilla
		try {
			dispatcher.execute(input, source);
		} catch (CommandSyntaxException exception) {
			Skript.getInstance().getSLF4JLogger().info("Command syntax error", exception);
		}
		return true;
	}

	@Override
	public @Unmodifiable Set<CommandSourceType> supportedTypes() {
		return Set.of(
			CommandSourceType.simple(BukkitCommandSender.class, sender -> sender.wrapped() instanceof Player,
				"player", "the player", "players", "the players"),
			CommandSourceType.simple(BukkitCommandSender.class,
				sender -> sender.wrapped() instanceof ConsoleCommandSender,
				"console", "the console", "server", "the server"),
			CommandSourceType.simple(BukkitCommandSender.class, sender -> sender.wrapped() instanceof BlockCommandSender,
				"block", "blocks", "command block", "command blocks"),
			CommandSourceType.simple(BukkitCommandSender.class, sender -> sender.wrapped() instanceof Entity,
				"entity", "entities")
		);
	}

	private static final @Nullable MethodHandle NODE_CHILDREN_GETTER;

	static {
		MethodHandle nodeChildrenGetter = null;
		try {
			nodeChildrenGetter = MethodHandles.privateLookupIn(CommandNode.class, MethodHandles.lookup())
				.unreflectGetter(CommandNode.class.getDeclaredField("children"));
		} catch (Exception exception) {
			if (Skript.debug())
				throw Skript.exception(exception, "Failed to access the command node children field");
		}
		NODE_CHILDREN_GETTER = nodeChildrenGetter;
	}

	@EventHandler
	@SuppressWarnings("UnstableApiUsage")
	private void onAsyncPlayerSendCommands(AsyncPlayerSendCommandsEvent<@NotNull CommandSourceStack> event)
			throws Throwable {
		if (!event.isAsynchronous() && event.hasFiredAsync())
			return;
		RootCommandNode<CommandSourceStack> rootNode = event.getCommandNode();
		Consumer<CommandNode<CommandSourceStack>> bukkitNodeRemover = node -> {};
		if (NODE_CHILDREN_GETTER != null) {
			//noinspection unchecked
			Map<String, CommandNode<CommandSourceStack>> children = (Map<String, CommandNode<CommandSourceStack>>)
				NODE_CHILDREN_GETTER.invokeExact((CommandNode<CommandSourceStack>) rootNode);
			bukkitNodeRemover = node -> children.remove(node.getName());
		}

		for (RootSkriptCommandNode<BukkitCommandSender> node : commands.values()) {
			CommandNode<CommandSourceStack> paperCompatible = convertToClientsidePaper(node.flat());
			bukkitNodeRemover.accept(paperCompatible);
			rootNode.addChild(paperCompatible);
		}
	}

	@EventHandler
	private void onAsyncPlayerSendSuggestions(AsyncPlayerSendSuggestionsEvent event) throws Throwable {
		String label = getLabel(event.getBuffer());
		if (getCommand(label) == null)
			return;
		ensureDispatcher();
		assert dispatcher != null;
		ParseResults<BukkitCommandSender> parseResults =
			dispatcher.parse(event.getBuffer().substring(1), new BukkitCommandSender(event.getPlayer()));
		Suggestions suggestions = dispatcher.getCompletionSuggestions(parseResults).get();
		// add +1 for '/'
		StringRange range = new StringRange(suggestions.getRange().getStart() + 1, suggestions.getRange().getEnd() + 1);
		event.setSuggestions(new Suggestions(range, suggestions.getList()));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	private void onAsyncTabComplete(AsyncTabCompleteEvent event) {
		if (getCommand(getLabel(event.getBuffer())) == null)
			return;
		event.setCompletions(Collections.singletonList("placeholder"));
		event.setHandled(true);
	}

	private static String getLabel(String buffer) {
		String label = buffer.split(" ")[0];
		if (label.startsWith("/"))
			return label.substring(1);
		return label;
	}

	/**
	 * Converts given command node to a node that can be safely sent to the client.
	 *
	 * @param node node to convert
	 * @return node that can be sent to the client
	 */
	// TODO convert paper argument types to NMS argument types
	// TODO filter out commands with requirements player does not meet (mirrors vanilla behaviour)
	private CommandNode<CommandSourceStack> convertToClientsidePaper(CommandNode<BukkitCommandSender> node) {
		ArgumentBuilder<CommandSourceStack, ?> builder;
		if (node instanceof LiteralCommandNode<BukkitCommandSender> lcn) {
			builder = LiteralArgumentBuilder.literal(lcn.getLiteral());
		} else if (node instanceof ArgumentCommandNode<?,?> acn) {
			//noinspection unchecked
			builder = RequiredArgumentBuilder.<CommandSourceStack, Object>argument(acn.getName(),
					(ArgumentType<Object>) acn.getType())
				// We provide dummy suggestions for all argument command nodes.
				// Such nodes are then marked as 'Has suggestions type' on the client and
				// always asked for tab completions, handled by the listener in this class.
				.suggests((ctx, b) -> Suggestions.empty());
		} else {
			throw new IllegalArgumentException("Unsupported node implementation; only native Brigadier nodes "
				+ "are supported");
		}
		if (node.getRequirement() != null)
			builder.requires(stack -> true);
		if (node.getRedirect() != null)
			builder.forward(convertToClientsidePaper(node.getRedirect()),
				ctx -> Collections.emptyList(), node.isFork());
		if (node.getCommand() != null)
			builder.executes(ctx -> com.mojang.brigadier.Command.SINGLE_SUCCESS);
		node.getChildren().forEach(child -> builder.then(convertToClientsidePaper(child)));
		return builder.build();
	}

	private class WrappedBrigadierCommand extends Command {

		protected WrappedBrigadierCommand(RootSkriptCommandNode<BukkitCommandSender> node) {
			super(node.getName(), node.getDescription() != null ? node.getDescription() : "",
				"" /* TODO usage */, new ArrayList<>(node.getAliases()));
			// TODO permission, permission message
		}

		@Override
		public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
			return PaperCommandHandler.this.dispatchCommand(new BukkitCommandSender(sender),
				(getName() + " " + String.join(" ", args)).trim());
		}

	}

}
