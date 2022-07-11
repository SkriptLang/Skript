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

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.util.SkriptColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.SimplePluginManager;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Callback;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

//TODO option to disable replacement of <color>s in command arguments?

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public abstract class Commands {
	
	public final static ArgsMessage m_too_many_arguments = new ArgsMessage("commands.too many arguments");
	public final static Message m_internal_error = new Message("commands.internal error");
	public final static Message m_correct_usage = new Message("commands.correct usage");
	
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
				final Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
				commandMapField.setAccessible(true);
				commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());
				
				final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				cmKnownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
				
				try {
					final Field aliasesField = SimpleCommandMap.class.getDeclaredField("aliases");
					aliasesField.setAccessible(true);
					cmAliases = (Set<String>) aliasesField.get(commandMap);
				} catch (final NoSuchFieldException e) {}
			}
		} catch (final SecurityException e) {
			Skript.error("Please disable the security manager");
			commandMap = null;
		} catch (final Exception e) {
			Skript.outdatedError(e);
			commandMap = null;
		}
	}
	
	private final static SectionValidator commandStructure = new SectionValidator()
			.addEntry("usage", true)
			.addEntry("description", true)
			.addEntry("permission", true)
			.addEntry("permission message", true)
			.addEntry("cooldown", true)
			.addEntry("cooldown message", true)
			.addEntry("cooldown bypass", true)
			.addEntry("cooldown storage", true)
			.addEntry("aliases", true)
			.addEntry("executable by", true)
			.addSection("trigger", false);
	
	@Nullable
	public static List<Argument<?>> currentArguments = null;
	
	@SuppressWarnings("null")
	private final static Pattern escape = Pattern.compile("[" + Pattern.quote("(|)<>%\\") + "]");
	@SuppressWarnings("null")
	private final static Pattern unescape = Pattern.compile("\\\\[" + Pattern.quote("(|)<>%\\") + "]");
	
	private static String escape(final String s) {
		return "" + escape.matcher(s).replaceAll("\\\\$0");
	}
	
	private static String unescape(final String s) {
		return "" + unescape.matcher(s).replaceAll("$0");
	}
	
	private final static Listener commandListener = new Listener() {
		@SuppressWarnings("null")
		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
			if (handleCommand(e.getPlayer(), e.getMessage().substring(1)))
				e.setCancelled(true);
		}
		
		@SuppressWarnings("null")
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onServerCommand(final ServerCommandEvent e) {
			if (e.getCommand() == null || e.getCommand().isEmpty() || e.isCancelled())
				return;
			if (SkriptConfig.enableEffectCommands.value() && e.getCommand().startsWith(SkriptConfig.effectCommandToken.value())) {
				if (handleEffectCommand(e.getSender(), e.getCommand())) {
					e.setCancelled(true);
				}
				return;
			}
		}
	};
	
	
	@Nullable
	private final static Listener pre1_3chatListener = Skript.classExists("org.bukkit.event.player.AsyncPlayerChatEvent") ? null : new Listener() {
		@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
		public void onPlayerChat(final PlayerChatEvent e) {
			if (!SkriptConfig.enableEffectCommands.value() || !e.getMessage().startsWith(SkriptConfig.effectCommandToken.value()))
				return;
			if (handleEffectCommand(e.getPlayer(), e.getMessage()))
				e.setCancelled(true);
		}
	};
	@Nullable
	private final static Listener post1_3chatListener = !Skript.classExists("org.bukkit.event.player.AsyncPlayerChatEvent") ? null : new Listener() {
		@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
		public void onPlayerChat(final AsyncPlayerChatEvent e) {
			if (!SkriptConfig.enableEffectCommands.value() || !e.getMessage().startsWith(SkriptConfig.effectCommandToken.value()))
				return;
			if (!e.isAsynchronous()) {
				if (handleEffectCommand(e.getPlayer(), e.getMessage()))
					e.setCancelled(true);
			} else {
				final Future<Boolean> f = Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return handleEffectCommand(e.getPlayer(), e.getMessage());
					}
				});
				try {
					while (true) {
						try {
							if (f.get())
								e.setCancelled(true);
							break;
						} catch (final InterruptedException e1) {}
					}
				} catch (final ExecutionException e1) {
					Skript.exception(e1);
				}
			}
		}
	};
	
	/**
	 * @param sender
	 * @param command full command string without the slash
	 * @return whether to cancel the event
	 */
	static boolean handleCommand(final CommandSender sender, final String command) {
		final String[] cmd = command.split("\\s+", 2);
		cmd[0] = cmd[0].toLowerCase(Locale.ENGLISH);
		if (cmd[0].endsWith("?")) {
			final ScriptCommand c = commands.get(cmd[0].substring(0, cmd[0].length() - 1));
			if (c != null) {
				c.sendHelp(sender);
				return true;
			}
		}
		final ScriptCommand c = commands.get(cmd[0]);
		if (c != null) {
//			if (cmd.length == 2 && cmd[1].equals("?")) {
//				c.sendHelp(sender);
//				return true;
//			}
			if (SkriptConfig.logPlayerCommands.value() && sender instanceof Player)
				SkriptLogger.LOGGER.info(sender.getName() + " [" + ((Player) sender).getUniqueId() + "]: /" + command);
			c.execute(sender, "" + cmd[0], cmd.length == 1 ? "" : "" + cmd[1]);
			return true;
		}
		return false;
	}
	
	static boolean handleEffectCommand(final CommandSender sender, String command) {
		if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("skript.effectcommands") || SkriptConfig.allowOpsToUseEffectCommands.value() && sender.isOp()))
			return false;
		try {
			command = "" + command.substring(SkriptConfig.effectCommandToken.value().length()).trim();
			final RetainingLogHandler log = SkriptLogger.startRetainingLog();
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
						if (SkriptConfig.logPlayerCommands.value() && !(sender instanceof ConsoleCommandSender))
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
		} catch (final Exception e) {
			Skript.exception(e, "Unexpected error while executing effect command '" + SkriptColor.replaceColorChar(command) + "' by '" + sender.getName() + "'");
			sender.sendMessage(ChatColor.RED + "An internal error occurred while executing this effect. Please refer to the server log for details.");
			return true;
		}
	}
	
	@SuppressWarnings("null")
	private final static Pattern commandPattern = Pattern.compile("(?i)^command /?(\\S+)\\s*(\\s+(.+))?$"),
			argumentPattern = Pattern.compile("<\\s*(?:([^>]+?)\\s*:\\s*)?(.+?)\\s*(?:=\\s*(" + SkriptParser.wildcard + "))?\\s*>");
	
	@Nullable
	public static ScriptCommand loadCommand(final SectionNode node) {
		return loadCommand(node, true);
	}
	
	@Nullable
	public static ScriptCommand loadCommand(final SectionNode node, final boolean alsoRegister) {
		final String key = node.getKey();
		if (key == null)
			return null;
		final String s = ScriptLoader.replaceOptions(key);
		
		int level = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '[') {
				level++;
			} else if (s.charAt(i) == ']') {
				if (level == 0) {
					Skript.error("Invalid placement of [optional brackets]");
					return null;
				}
				level--;
			}
		}
		if (level > 0) {
			Skript.error("Invalid amount of [optional brackets]");
			return null;
		}
		
		Matcher m = commandPattern.matcher(s);
		final boolean a = m.matches();
		assert a;

		final String command = "" + m.group(1).toLowerCase(Locale.ENGLISH);
		final ScriptCommand existingCommand = commands.get(command);
		if (alsoRegister && existingCommand != null && existingCommand.getLabel().equals(command)) {
			final File f = existingCommand.getScript();
			Skript.error("A command with the name /" + existingCommand.getName() + " is already defined" + (f == null ? "" : " in " + f.getName()));
			return null;
		}
		
		final String arguments = m.group(3) == null ? "" : m.group(3);
		final StringBuilder pattern = new StringBuilder();
		
		List<Argument<?>> currentArguments = Commands.currentArguments = new ArrayList<>(); //Mirre
		m = argumentPattern.matcher(arguments);
		int lastEnd = 0;
		int optionals = 0;
		for (int i = 0; m.find(); i++) {
			pattern.append(escape("" + arguments.substring(lastEnd, m.start())));
			optionals += StringUtils.count(arguments, '[', lastEnd, m.start());
			optionals -= StringUtils.count(arguments, ']', lastEnd, m.start());
			
			lastEnd = m.end();
			
			ClassInfo<?> c;
			c = Classes.getClassInfoFromUserInput("" + m.group(2));
			final NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + m.group(2));
			if (c == null)
				c = Classes.getClassInfoFromUserInput(p.getFirst());
			if (c == null) {
				Skript.error("Unknown type '" + m.group(2) + "'");
				return null;
			}
			final Parser<?> parser = c.getParser();
			if (parser == null || !parser.canParse(ParseContext.COMMAND)) {
				Skript.error("Can't use " + c + " as argument of a command");
				return null;
			}
			
			final Argument<?> arg = Argument.newInstance(m.group(1), c, m.group(3), i, !p.getSecond(), optionals > 0);
			if (arg == null)
				return null;
			currentArguments.add(arg);
			
			if (arg.isOptional() && optionals == 0) {
				pattern.append('[');
				optionals++;
			}
			pattern.append("%" + (arg.isOptional() ? "-" : "") + Utils.toEnglishPlural(c.getCodeName(), p.getSecond()) + "%");
		}
		
		pattern.append(escape("" + arguments.substring(lastEnd)));
		optionals += StringUtils.count(arguments, '[', lastEnd);
		optionals -= StringUtils.count(arguments, ']', lastEnd);
		for (int i = 0; i < optionals; i++)
			pattern.append(']');
		
		String desc = "/" + command + " ";

		desc += StringUtils.replaceAll(pattern, "(?<!\\\\)%-?(.+?)%", m1 -> {
			assert m1 != null;
			NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + m1.group(1));
			String s1 = p.getFirst();
			return "<" + Classes.getClassInfo(s1).getName().toString(p.getSecond()) + ">";
		});

		desc = unescape(desc);
		desc = "" + desc.trim();
		
		node.convertToEntries(0);
		commandStructure.validate(node);
		if (!(node.get("trigger") instanceof SectionNode))
			return null;
		
		final String usage = ScriptLoader.replaceOptions(node.get("usage", m_correct_usage + " " + desc));
		final String description = ScriptLoader.replaceOptions(node.get("description", ""));
		ArrayList<String> aliases = new ArrayList<>(Arrays.asList(ScriptLoader.replaceOptions(node.get("aliases", "")).split("\\s*,\\s*/?")));
		if (aliases.get(0).startsWith("/"))
			aliases.set(0, aliases.get(0).substring(1));
		else if (aliases.get(0).isEmpty())
			aliases = new ArrayList<>(0);
		final String permission = ScriptLoader.replaceOptions(node.get("permission", ""));

		String rawPermissionMessage = ScriptLoader.replaceOptions(node.get("permission message", ""))
			.replace("\"", "\"\"");

		VariableString permissionMessage = rawPermissionMessage.isEmpty() ?
			null : VariableString.newInstance(rawPermissionMessage);

		final SectionNode trigger = (SectionNode) node.get("trigger");
		if (trigger == null)
			return null;
		final String[] by = ScriptLoader.replaceOptions(node.get("executable by", "console,players")).split("\\s*,\\s*|\\s+(and|or)\\s+");
		int executableBy = 0;
		for (final String b : by) {
			if (b.equalsIgnoreCase("console") || b.equalsIgnoreCase("the console")) {
				executableBy |= ScriptCommand.CONSOLE;
			} else if (b.equalsIgnoreCase("players") || b.equalsIgnoreCase("player")) {
				executableBy |= ScriptCommand.PLAYERS;
			} else {
				Skript.warning("'executable by' should be either be 'players', 'console', or both, but found '" + b + "'");
			}
		}

		final String cooldownString = ScriptLoader.replaceOptions(node.get("cooldown", ""));
		Timespan cooldown = null;
		if (!cooldownString.isEmpty()) {
			// ParseContext doesn't matter for Timespan's parser
			cooldown = Classes.parse(cooldownString, Timespan.class, ParseContext.DEFAULT);
			if (cooldown == null) {
				Skript.warning("'" + cooldownString + "' is an invalid timespan for the cooldown");
			}
		}

		String cooldownMessageString = ScriptLoader.replaceOptions(node.get("cooldown message", ""))
			.replace("\"", "\"\"");
		boolean usingCooldownMessage = !cooldownMessageString.isEmpty();
		VariableString cooldownMessage = null;
		if (usingCooldownMessage) {
			cooldownMessage = VariableString.newInstance(cooldownMessageString);
		}

		String cooldownBypass = ScriptLoader.replaceOptions(node.get("cooldown bypass", ""));

		if (permissionMessage != null && permission.isEmpty()) {
			Skript.warning("command /" + command + " has a permission message set, but not a permission");
		}

		if (usingCooldownMessage && cooldownString.isEmpty()) {
			Skript.warning("command /" + command + " has a cooldown message set, but not a cooldown");
		}

		String cooldownStorageString = ScriptLoader.replaceOptions(node.get("cooldown storage", ""));
		VariableString cooldownStorage = null;
		if (!cooldownStorageString.isEmpty()) {
			cooldownStorage = VariableString.newInstance(cooldownStorageString, StringMode.VARIABLE_NAME);
		}

		if (Skript.debug() || node.debug())
			Skript.debug("command " + desc + ":");
		
		final File config = node.getConfig().getFile();
		if (config == null) {
			assert false;
			return null;
		}
		
		Commands.currentArguments = currentArguments;
		ScriptCommand c;
		try {
			c = new ScriptCommand(config, command, pattern.toString(), currentArguments, description, usage,
					aliases, permission, permissionMessage, cooldown, cooldownMessage, cooldownBypass, cooldownStorage,
					executableBy, ScriptLoader.loadItems(trigger));
			c.trigger.setLineNumber(node.getLine());
		} finally {
			Commands.currentArguments = null;
		}
		
		if (alsoRegister)
			registerCommand(c);
		
		if (Skript.logVeryHigh() && !Skript.debug())
			Skript.info("registered command " + desc);
		return c;
	}
	
	public static boolean skriptCommandExists(final String command) {
		final ScriptCommand c = commands.get(command);
		return c != null && c.getName().equals(command);
	}
	
	public static void registerCommand(final ScriptCommand command) {
		// Validate that there are no duplicates
		final ScriptCommand existingCommand = commands.get(command.getLabel());
		if (existingCommand != null && existingCommand.getLabel().equals(command.getLabel())) {
			final File f = existingCommand.getScript();
			Skript.error("A command with the name /" + existingCommand.getName() + " is already defined" + (f == null ? "" : " in " + f.getName()));
			return;
		}
		
		if (commandMap != null) {
			assert cmKnownCommands != null;// && cmAliases != null;
			command.register(commandMap, cmKnownCommands, cmAliases);
		}
		commands.put(command.getLabel(), command);
		for (final String alias : command.getActiveAliases()) {
			commands.put(alias.toLowerCase(Locale.ENGLISH), command);
		}
		command.registerHelp();
	}
	
	public static int unregisterCommands(final File script) {
		int numCommands = 0;
		final Iterator<ScriptCommand> commandsIter = commands.values().iterator();
		while (commandsIter.hasNext()) {
			final ScriptCommand c = commandsIter.next();
			if (script.equals(c.getScript())) {
				numCommands++;
				c.unregisterHelp();
				if (commandMap != null) {
					assert cmKnownCommands != null;// && cmAliases != null;
					c.unregister(commandMap, cmKnownCommands, cmAliases);
				}
				commandsIter.remove();
			}
		}
		return numCommands;
	}
	
	private static boolean registeredListeners = false;
	
	public static void registerListeners() {
		if (!registeredListeners) {
			Bukkit.getPluginManager().registerEvents(commandListener, Skript.getInstance());
			
			Listener post13Listener = post1_3chatListener;
			Listener pre13Listener = pre1_3chatListener;
			if (post13Listener != null) {
				Bukkit.getPluginManager().registerEvents(post13Listener, Skript.getInstance());
			} else {
				assert pre13Listener != null;
				Bukkit.getPluginManager().registerEvents(pre13Listener, Skript.getInstance());
			}
			registeredListeners = true;
		}
	}
	
	public static void clearCommands() {
		final SimpleCommandMap commandMap = Commands.commandMap;
		if (commandMap != null) {
			final Map<String, Command> cmKnownCommands = Commands.cmKnownCommands;
			final Set<String> cmAliases = Commands.cmAliases;
			assert cmKnownCommands != null;// && cmAliases != null;
			for (final ScriptCommand c : commands.values())
				c.unregister(commandMap, cmKnownCommands, cmAliases);
		}
		for (final ScriptCommand c : commands.values()) {
			c.unregisterHelp();
		}
		commands.clear();
	}
	
	/**
	 * copied from CraftBukkit (org.bukkit.craftbukkit.help.CommandAliasHelpTopic)
	 */
	public final static class CommandAliasHelpTopic extends HelpTopic {
		
		private final String aliasFor;
		private final HelpMap helpMap;
		
		public CommandAliasHelpTopic(final String alias, final String aliasFor, final HelpMap helpMap) {
			this.aliasFor = aliasFor.startsWith("/") ? aliasFor : "/" + aliasFor;
			this.helpMap = helpMap;
			name = alias.startsWith("/") ? alias : "/" + alias;
			Validate.isTrue(!name.equals(this.aliasFor), "Command " + name + " cannot be alias for itself");
			shortText = ChatColor.YELLOW + "Alias for " + ChatColor.WHITE + this.aliasFor;
		}
		
		@Override
		public String getFullText(final CommandSender forWho) {
			final StringBuilder sb = new StringBuilder(shortText);
			final HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
			if (aliasForTopic != null) {
				sb.append("\n");
				sb.append(aliasForTopic.getFullText(forWho));
			}
			return "" + sb.toString();
		}
		
		@Override
		public boolean canSee(final CommandSender commandSender) {
			if (amendedPermission == null) {
				final HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
				if (aliasForTopic != null) {
					return aliasForTopic.canSee(commandSender);
				} else {
					return false;
				}
			} else {
				assert amendedPermission != null;
				return commandSender.hasPermission(amendedPermission);
			}
		}
	}
	
}
