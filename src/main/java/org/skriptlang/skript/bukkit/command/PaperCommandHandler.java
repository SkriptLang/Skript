package org.skriptlang.skript.bukkit.command;

import ch.njol.skript.Skript;
import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.brigadier.RootSkriptCommandNode;
import org.skriptlang.skript.lang.command.CommandHandler;
import org.skriptlang.skript.lang.command.CommandSourceType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Command Handler implementation for Paper environment.
 */
public class PaperCommandHandler implements CommandHandler<BukkitCommandSender>, Listener {

	private final Map<String, RootSkriptCommandNode<BukkitCommandSender>> commands = new ConcurrentHashMap<>();
	private @Nullable CommandDispatcher<BukkitCommandSender> dispatcher;

	@Override
	public synchronized boolean registerCommand(RootSkriptCommandNode<BukkitCommandSender> command) {
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
	public synchronized boolean unregisterCommand(RootSkriptCommandNode<BukkitCommandSender> command) {
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
		if (dispatcher == null) {
			dispatcher = new CommandDispatcher<>();
			commands.values().forEach(cmd -> dispatcher.register(cmd.flatToBuilder()));
		}

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

	private static final @Nullable Field NODE_CHILDREN_FIELD;

	static {
		Field nodeChildrenField = null;
		try {
			nodeChildrenField = CommandNode.class.getDeclaredField("children");
			nodeChildrenField.setAccessible(true);
		} catch (Exception exception) {
			if (Skript.debug())
				throw Skript.exception(exception, "Failed to access the command node children field");
		}
		NODE_CHILDREN_FIELD = nodeChildrenField;
	}

	@EventHandler
	@SuppressWarnings("UnstableApiUsage")
	public void onAsyncPlayerSendCommands(AsyncPlayerSendCommandsEvent<@NotNull CommandSourceStack> event) {
		if (!event.isAsynchronous() && event.hasFiredAsync())
			return;
		RootCommandNode<CommandSourceStack> rootNode = event.getCommandNode();
		Consumer<CommandNode<CommandSourceStack>> bukkitNodeRemover = node -> {};
		try {
			Preconditions.checkNotNull(NODE_CHILDREN_FIELD);
			//noinspection unchecked
			Map<String, CommandNode<CommandSourceStack>> children = (Map<String, CommandNode<CommandSourceStack>>)
				NODE_CHILDREN_FIELD.get(rootNode);
			bukkitNodeRemover = node -> children.remove(node.getName());
		} catch (Exception exception) {
			if (Skript.debug())
				throw Skript.exception(exception, "Failed to access the node children field");
		}

		for (RootSkriptCommandNode<BukkitCommandSender> node : commands.values()) {
			CommandNode<CommandSourceStack> paperCompatible = convertToClientsidePaper(node.flat());
			bukkitNodeRemover.accept(paperCompatible);
			rootNode.addChild(paperCompatible);
		}
	}

	// TODO suggestions

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
			builder = RequiredArgumentBuilder.argument(acn.getName(), acn.getType());
		} else {
			throw new IllegalArgumentException("Unsupported node implementation; only native Brigadier nodes "
				+ "are supported");
		}
		if (node.getRequirement() != null)
			builder.requires(stack -> true);
		if (node.getRedirect() != null)
			builder.forward(convertToClientsidePaper(node.getRedirect()), ctx -> Collections.emptyList(), node.isFork());
		if (node.getCommand() != null)
			builder.executes(ctx -> com.mojang.brigadier.Command.SINGLE_SUCCESS);
		node.getChildren().forEach(child -> builder.then(convertToClientsidePaper(child)));
		return builder.build();
	}

	private class WrappedBrigadierCommand extends Command {

		protected WrappedBrigadierCommand(RootSkriptCommandNode<BukkitCommandSender> node) {
			super(node.getName(), node.getDescription() != null ? node.getDescription() : "",
				"" /* TODO usage */, new ArrayList<>(node.getAliases()));
		}

		@Override
		public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
			return PaperCommandHandler.this.dispatchCommand(new BukkitCommandSender(sender),
				getName() + " " + String.join(" ", args));
		}

	}

}
