package org.skriptlang.skript.commands.bukkit;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.api.Argument;
import org.skriptlang.skript.commands.api.CommandHandler;
import org.skriptlang.skript.commands.api.ScriptCommand;
import org.skriptlang.skript.commands.api.ScriptCommandSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BukkitCommandHandler implements CommandHandler {

	private final Map<ScriptCommand, Command> scriptCommandMap = new HashMap<>();

	private final SimpleCommandMap bukkitCommandMap;
	private final Map<String, Command> bukkitKnownCommands;

	@SuppressWarnings("unchecked")
	public BukkitCommandHandler() {
		try {
			Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			bukkitCommandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());

			Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
			knownCommandsField.setAccessible(true);
			bukkitKnownCommands = (Map<String, Command>) knownCommandsField.get(bukkitCommandMap);
		} catch (SecurityException e) {
			throw new SkriptAPIException("Failed to access the command map. Please disable the security manager", e);
		} catch (Exception e) {
			throw new SkriptAPIException("Failed to access the command map. Please ensure your Skript installation is up to date!");
		}
	}

	@Override
	public boolean registerCommand(ScriptCommand scriptCommand) {
		synchronized (scriptCommandMap) {
			PluginCommand command = createPluginCommand(scriptCommand.getLabel());

			command.setAliases(scriptCommand.getAliases());
			command.setDescription(scriptCommand.getDescription());
			command.setUsage(scriptCommand.getUsage().getUsage());

			command.setExecutor(new TabExecutor() {
				@Override
				public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
					ScriptCommandSender scriptSender = new BukkitCommandSender(sender);
					scriptCommand.execute(scriptSender, label, args);
					return true;
				}

				@Override
				public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
					int argIndex = args.length - 1;
					List<Argument<?>> arguments = scriptCommand.getArguments();

					if (argIndex >= arguments.size()) { // Too many arguments, nothing to complete
						return Collections.emptyList();
					}

					// Attempt to use default completion in some cases
					if (OfflinePlayer.class.isAssignableFrom(arguments.get(argIndex).getType())) {
						return null;
					}

					// TODO suggest all values of ClassInfo if possible
					return Collections.emptyList(); // No tab completion here!
				}
			});

			bukkitCommandMap.register(scriptCommand.getNamespace(), command);
			scriptCommandMap.put(scriptCommand, command);
		}

		return true;
	}

	@Override
	public boolean unregisterCommand(ScriptCommand scriptCommand) {
		synchronized (scriptCommandMap) {
			Command command = scriptCommandMap.get(scriptCommand);
			if (command == null) {
				return false;
			}
			scriptCommandMap.remove(scriptCommand);

			bukkitKnownCommands.remove(scriptCommand.getLabel());
			bukkitKnownCommands.remove(scriptCommand.getNamespace() + ":" + scriptCommand.getLabel());

			return command.unregister(bukkitCommandMap);
		}
	}

	@Override
	public @Nullable ScriptCommand getScriptCommand(String label) {
		synchronized (scriptCommandMap) {
			for (ScriptCommand scriptCommand : scriptCommandMap.keySet()) {
				if (scriptCommand.getLabel().equals(label)) {
					return scriptCommand;
				}
			}
		}
		return null;
	}

	@Override
	public Collection<String> getScriptCommands() {
		synchronized (scriptCommandMap) {
			return scriptCommandMap.keySet().parallelStream()
				.map(ScriptCommand::getLabel)
				.collect(Collectors.toSet());
		}
	}

	@Override
	public Collection<String> getServerCommands() {
		synchronized (scriptCommandMap) {
			return bukkitKnownCommands.values().parallelStream()
				.map(Command::getLabel)
				.collect(Collectors.toSet());
		}
	}

	private static PluginCommand createPluginCommand(String label) {
		try {
			Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constructor.setAccessible(true);
			PluginCommand bukkitCommand = constructor.newInstance(label, Skript.getInstance());
			bukkitCommand.setLabel(label);
			return bukkitCommand;
		} catch (Exception e) {
			Skript.outdatedError(e);
			throw new SkriptAPIException("Failed to create plugin command", e);
		}
	}

}
