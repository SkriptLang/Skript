/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.variables.Variables;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.SimplePluginManager;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

//TODO option to disable replacement of <color>s in command arguments?

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public abstract class Commands {

	public final static ArgsMessage m_too_many_arguments = new ArgsMessage("commands.too many arguments");
	public final static Message m_internal_error = new Message("commands.internal error");
	public final static Message m_correct_usage = new Message("commands.correct usage");

	/**
	 * A Converter flag declaring that a Converter cannot be used for parsing command arguments.
	 */
	public static final int CONVERTER_NO_COMMAND_ARGUMENTS = 4;

	private final static Map<String, ScriptCommand> commands = new HashMap<>();

	@Nullable
	private static SimpleCommandMap commandMap = null;
	@Nullable
	private static Map<String, Command> cmKnownCommands;
	@Nullable
	private static Set<String> cmAliases;

	static {
		init(); // separate method for the annotation
	}
	public static Set<String> getScriptCommands(){
		return commands.keySet();
	}

	@Nullable
	public static SimpleCommandMap getCommandMap(){
		return commandMap;
	}

	@SuppressWarnings("unchecked")
	private static void init() {
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
				Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
				commandMapField.setAccessible(true);
				commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());

				Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				cmKnownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

				try {
					Field aliasesField = SimpleCommandMap.class.getDeclaredField("aliases");
					aliasesField.setAccessible(true);
					cmAliases = (Set<String>) aliasesField.get(commandMap);
				} catch (NoSuchFieldException ignored) {}
			}
		} catch (SecurityException e) {
			Skript.error("Please disable the security manager");
			commandMap = null;
		} catch (Exception e) {
			Skript.outdatedError(e);
			commandMap = null;
		}
	}

	@Nullable
	public static List<Argument<?>> currentArguments = null;

	@SuppressWarnings("null")
	private final static Pattern escape = Pattern.compile("[" + Pattern.quote("(|)<>%\\") + "]");
	@SuppressWarnings("null")
	private final static Pattern unescape = Pattern.compile("\\\\[" + Pattern.quote("(|)<>%\\") + "]");

	public static String escape(String string) {
		return "" + escape.matcher(string).replaceAll("\\\\$0");
	}

	public static String unescape(String string) {
		return "" + unescape.matcher(string).replaceAll("$0");
	}

	private final static Listener commandListener = new Listener() {

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
			// Spigot will simply report that the command doesn't exist if a player does not have permission to use it.
			// This is good security but, well, it's a breaking change for Skript. So we need to check for permissions
			// ourselves and handle those messages, for every command.

			// parse command, see if it's a skript command
			String[] cmd = event.getMessage().substring(1).split("\\s+", 2);
			String label = cmd[0].toLowerCase(Locale.ENGLISH);
			String arguments = cmd.length == 1 ? "" : "" + cmd[1];
			ScriptCommand command = commands.get(label);

			// if so, check permissions
			if (command != null && !command.checkPermissions(event.getPlayer(), label, arguments))
				event.setCancelled(true);
		}

		@SuppressWarnings("null")
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onServerCommand(ServerCommandEvent event) {
			if (event.getCommand().isEmpty() || event.isCancelled())
				return;
			if ((Skript.testing() || SkriptConfig.enableEffectCommands.value()) && event.getCommand().startsWith(SkriptConfig.effectCommandToken.value())) {
				if (handleEffectCommand(event.getSender(), event.getCommand()))
					event.setCancelled(true);
			}
		}
	};

	static boolean handleEffectCommand(CommandSender sender, String command) {
		if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("skript.effectcommands") || SkriptConfig.allowOpsToUseEffectCommands.value() && sender.isOp()))
			return false;
		try {
			command = "" + command.substring(SkriptConfig.effectCommandToken.value().length()).trim();
			RetainingLogHandler log = SkriptLogger.startRetainingLog();
			try {
				// Call the event on the Bukkit API for addon developers.
				EffectCommandEvent effectCommand = new EffectCommandEvent(sender, command);
				Bukkit.getPluginManager().callEvent(effectCommand);
				command = effectCommand.getCommand();
				ParserInstance parserInstance = ParserInstance.get();
				parserInstance.setCurrentEvent("effect command", EffectCommandEvent.class);
				Effect effect = Effect.parse(command, null);
				parserInstance.deleteCurrentEvent();

				if (effect != null) {
					log.clear(); // ignore warnings and stuff
					log.printLog();
					if (!effectCommand.isCancelled()) {
						sender.sendMessage(ChatColor.GRAY + "executing '" + SkriptColor.replaceColorChar(command) + "'");
						// TODO: remove logPlayerCommands for 2.8.0
						if ((SkriptConfig.logEffectCommands.value() || SkriptConfig.logPlayerCommands.value()) && !(sender instanceof ConsoleCommandSender))
							Skript.info(sender.getName() + " issued effect command: " + SkriptColor.replaceColorChar(command));
						TriggerItem.walk(effect, effectCommand);
						Variables.removeLocals(effectCommand);
					} else {
						sender.sendMessage(ChatColor.RED + "your effect command '" + SkriptColor.replaceColorChar(command) + "' was cancelled.");
					}
				} else {
					if (sender == Bukkit.getConsoleSender()) // log as SEVERE instead of INFO like printErrors below
						SkriptLogger.LOGGER.severe("Error in: " + SkriptColor.replaceColorChar(command));
					else
						sender.sendMessage(ChatColor.RED + "Error in: " + ChatColor.GRAY + SkriptColor.replaceColorChar(command));
					log.printErrors(sender, "(No specific information is available)");
				}
			} finally {
				log.stop();
			}
			return true;
		} catch (Exception e) {
			Skript.exception(e, "Unexpected error while executing effect command '" + SkriptColor.replaceColorChar(command) + "' by '" + sender.getName() + "'");
			sender.sendMessage(ChatColor.RED + "An internal error occurred while executing this effect. Please refer to the server log for details.");
			return true;
		}
	}

	@Nullable
	public static ScriptCommand getScriptCommand(String key) {
		return commands.get(key);
	}

	/*
	 * @deprecated Use {@link #scriptCommandExists(String)} instead.
	 */
	@Deprecated
	public static boolean skriptCommandExists(String command) {
		return scriptCommandExists(command);
	}

	public static boolean scriptCommandExists(String command) {
		ScriptCommand scriptCommand = commands.get(command);
		return scriptCommand != null && scriptCommand.getName().equals(command);
	}

	public static void registerCommand(ScriptCommand command) {
		// Validate that there are no duplicates
		ScriptCommand existingCommand = commands.get(command.getLabel());
		if (existingCommand != null && existingCommand.getLabel().equals(command.getLabel())) {
			Script script = existingCommand.getScript();
			Skript.error("A command with the name /" + existingCommand.getName() + " is already defined"
				+ (script != null ? (" in " + script.getConfig().getFileName()) : "")
			);
			return;
		}

		if (commandMap != null) {
			assert cmKnownCommands != null;// && cmAliases != null;
			command.register(commandMap, cmKnownCommands, cmAliases);
		}
		commands.put(command.getLabel(), command);
		for (String alias : command.getActiveAliases()) {
			commands.put(alias.toLowerCase(Locale.ENGLISH), command);
		}
		command.registerHelp();
	}

	@Deprecated
	public static int unregisterCommands(File script) {
		int numCommands = 0;
		for (ScriptCommand c : new ArrayList<>(commands.values())) {
			if (c.getScript() != null && c.getScript().equals(ScriptLoader.getScript(script))) {
				numCommands++;
				unregisterCommand(c);
			}
		}
		return numCommands;
	}

	public static void unregisterCommand(ScriptCommand scriptCommand) {
		scriptCommand.unregisterHelp();
		if (commandMap != null) {
			assert cmKnownCommands != null;// && cmAliases != null;
			scriptCommand.unregister(commandMap, cmKnownCommands, cmAliases);
		}
		commands.values().removeIf(command -> command == scriptCommand);
	}

	private static boolean registeredListeners = false;

	public static void registerListeners() {
		if (!registeredListeners) {
			Bukkit.getPluginManager().registerEvents(commandListener, Skript.getInstance());

			Bukkit.getPluginManager().registerEvents(new Listener() {
				@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
				public void onPlayerChat(AsyncPlayerChatEvent event) {
					if (!SkriptConfig.enableEffectCommands.value() || !event.getMessage().startsWith(SkriptConfig.effectCommandToken.value()))
						return;
					if (!event.isAsynchronous()) {
						if (handleEffectCommand(event.getPlayer(), event.getMessage()))
							event.setCancelled(true);
					} else {
						Future<Boolean> f = Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), () -> handleEffectCommand(event.getPlayer(), event.getMessage()));
						try {
							while (true) {
								try {
									if (f.get())
										event.setCancelled(true);
									break;
								} catch (InterruptedException ignored) {
								}
							}
						} catch (ExecutionException e) {
							Skript.exception(e);
						}
					}
				}
			}, Skript.getInstance());

			registeredListeners = true;
		}
	}

	/**
	 * copied from CraftBukkit (org.bukkit.craftbukkit.help.CommandAliasHelpTopic)
	 */
	public static final class CommandAliasHelpTopic extends HelpTopic {

		private final String aliasFor;
		private final HelpMap helpMap;

		public CommandAliasHelpTopic(String alias, String aliasFor, HelpMap helpMap) {
			this.aliasFor = aliasFor.startsWith("/") ? aliasFor : "/" + aliasFor;
			this.helpMap = helpMap;
			name = alias.startsWith("/") ? alias : "/" + alias;
			Validate.isTrue(!name.equals(this.aliasFor), "Command " + name + " cannot be alias for itself");
			shortText = ChatColor.YELLOW + "Alias for " + ChatColor.WHITE + this.aliasFor;
		}

		@Override
		@NotNull
		public String getFullText(CommandSender forWho) {
			StringBuilder fullText = new StringBuilder(shortText);
			HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
			if (aliasForTopic != null) {
				fullText.append("\n");
				fullText.append(aliasForTopic.getFullText(forWho));
			}
			return "" + fullText;
		}

		@Override
		public boolean canSee(CommandSender commandSender) {
			if (amendedPermission != null)
				return commandSender.hasPermission(amendedPermission);
			HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
			return aliasForTopic != null && aliasForTopic.canSee(commandSender);
		}
	}

}
