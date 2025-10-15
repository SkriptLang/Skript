package ch.njol.skript.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.command.Commands.CommandAliasHelpTopic;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.codehaus.plexus.util.cli.Arg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * This class is used for user-defined commands.
 */
public class ScriptCommand implements TabExecutor {

	public static final String m_executable_by_players = new Message("commands.executable by players").toString();
	public static final String m_executable_by_console = new Message("commands.executable by console").toString();
	public final static int PLAYERS = 0x1, CONSOLE = 0x2, BOTH = PLAYERS | CONSOLE;
	private static final String DEFAULT_PREFIX = "skript";

	private @Nullable Expression<String> cooldownStorage;
	private @Nullable List<Argument<?>> arguments;
	private @Nullable String cooldownBypass;
	private @Nullable List<String> aliases;
	private @Nullable CommandUsage usage;
	private @Nullable String description;
	private @Nullable Timespan cooldown;
	private @Nullable String permission;

	private final Expression<String> cooldownMessage;
	private final VariableString permissionMessage;
	private transient PluginCommand bukkitCommand;
	private final int executableBy;
	private final SectionNode node;
	private final Trigger trigger;
	private final String pattern;
	private final String prefix;
	private final Script script;
	private final String name;

	/**
	 * Constructs a ScriptCommand using the provided builder.
	 * @param builder the builder containing all command properties
	 */
	private ScriptCommand(Builder builder) {
		this.permissionMessage = builder.permissionMessage != null ? builder.permissionMessage : VariableString.newInstance(Language.get("commands.no permission message"));
		this.cooldownMessage = builder.cooldownMessage != null ? builder.cooldownMessage : new SimpleLiteral<>(Language.get("commands.cooldown message"), false);
		this.prefix = builder.prefix != null ? builder.prefix : DEFAULT_PREFIX;
		this.name = builder.name.toLowerCase(Locale.ROOT);
		this.cooldownStorage = builder.cooldownStorage;
		this.cooldownBypass = builder.cooldownBypass;
		this.executableBy = builder.executableBy;
		this.description = builder.description;
		this.permission = builder.permission;
		this.arguments = builder.arguments;
		this.cooldown = builder.cooldown;
		this.pattern = builder.pattern;
		this.aliases = builder.aliases;
		this.script = builder.script;
		this.usage = builder.usage;
		this.node = builder.node;

		if (aliases != null) {
			aliases.removeIf(name::equalsIgnoreCase);
		}

		HintManager hintManager = ParserInstance.get().getHintManager();
		try {
			hintManager.enterScope(false);
			for (Argument<?> argument : arguments) {
				String hintName = argument.getName();
				if (hintName == null) {
					continue;
				}
				if (!argument.isSingle()) {
					hintName += Variable.SEPARATOR + "*";
				}
				hintManager.set(hintName, argument.getType());
			}
			this.trigger = new Trigger(script, "command /" + name, new SimpleEvent(), ScriptLoader.loadItems(node));
			trigger.setLineNumber(node.getLine());
		} finally {
			hintManager.exitScope();
		}

		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			PluginCommand bukkitCommand = c.newInstance(name, Skript.getInstance());
			bukkitCommand.setAliases(aliases);
			bukkitCommand.setDescription(description);
			bukkitCommand.setLabel(name);
			bukkitCommand.setPermission(permission);
			// We can only set the message if it's simple (doesn't contains expressions)
			if (permissionMessage.isSimple())
				bukkitCommand.setPermissionMessage(permissionMessage.toString(null));
			bukkitCommand.setUsage(usage.getUsage());
			bukkitCommand.setExecutor(this);
		} catch (Exception e) {
			Skript.outdatedError(e);
			throw new EmptyStacktraceException();
		}
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
			@NotNull String label, @NotNull String @NotNull [] args) {
		assert args != null;
		int argIndex = args.length - 1;
		if (argIndex >= arguments.size())
			return Collections.emptyList();

		Argument<?> arg = arguments.get(argIndex);
		Class<?> argType = arg.getType();
		if (argType.equals(Player.class) || argType.equals(OfflinePlayer.class))
			return null;

		return Collections.emptyList();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String @NotNull [] args) {
		if (sender == null || label == null || args == null)
			return false;
		execute(sender, label, StringUtils.join(args, " "));
		return true;
	}

	public boolean execute(CommandSender sender, String commandLabel, String rest) {
		if (sender instanceof Player) {
			if ((executableBy & PLAYERS) == 0) {
				sender.sendMessage(m_executable_by_console);
				return false;
			}
		} else {
			if ((executableBy & CONSOLE) == 0) {
				sender.sendMessage(m_executable_by_players);
				return false;
			}
		}

		final ScriptCommandEvent event = new ScriptCommandEvent(ScriptCommand.this, sender, commandLabel, rest);

		if (!checkPermissions(sender, event))
			return false;

		if (sender instanceof Player && cooldown != null) {
			Player player = (Player) sender;
			UUID uuid = player.getUniqueId();
			if (cooldownBypass != null && !cooldownBypass.isEmpty() && player.hasPermission(cooldownBypass)) {
				setLastUsage(uuid, event, null);
			} else {
				Date lastUsage = getLastUsage(uuid, event);
				if (lastUsage != null) {
					if (getRemainingMilliseconds(uuid, event) > 0) {
						String msg = cooldownMessage.getSingle(event);
						if (msg != null)
							sender.sendMessage(msg);
						return false;
					} else if (!SkriptConfig.keepLastUsageDates.value()) {
						setLastUsage(uuid, event, null);
					}
				}
			}
		}

		Task.callSync(() -> {
			Date previousLastUsage = null;
			if (sender instanceof Player)
				previousLastUsage = getLastUsage(((Player) sender).getUniqueId(), event);

			execute2(event, sender, commandLabel, rest);

			if (sender instanceof Player && !event.isCooldownCancelled()) {
				Date lastUsage = getLastUsage(((Player) sender).getUniqueId(), event);
				if (Objects.equals(lastUsage, previousLastUsage))
					setLastUsage(((Player) sender).getUniqueId(), event, new Date());
			}
			return null;
		});

		return true; // Skript prints its own error message anyway
	}

	boolean execute2(final ScriptCommandEvent event, final CommandSender sender, final String commandLabel, final String rest) {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final boolean ok = SkriptParser.parseArguments(rest, ScriptCommand.this, event);
			if (!ok) {
				final LogEntry e = log.getError();
				if (e != null)
					sender.sendMessage(ChatColor.DARK_RED + e.toString());
				sender.sendMessage(usage.getUsage(event));
				log.clear();
				return false;
			}
			log.clearError();
		} finally {
			log.stop();
		}

		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + name + " " + rest);
		final long startTrigger = System.nanoTime();

		if (!trigger.execute(event))
			sender.sendMessage(Commands.m_internal_error.toString());

		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# " + name + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
		return true;
	}

	public boolean checkPermissions(CommandSender sender, String commandLabel, String arguments) {
		return checkPermissions(sender, new ScriptCommandEvent(this, sender, commandLabel, arguments));
	}

	public boolean checkPermissions(CommandSender sender, Event event) {
		if (!permission.isEmpty() && !sender.hasPermission(permission)) {
			if (sender instanceof Player) {
				List<MessageComponent> components = permissionMessage.getMessageComponents(event);
				((Player) sender).spigot().sendMessage(BungeeConverter.convert(components));
			} else {
				sender.sendMessage(permissionMessage.getSingle(event));
			}
			return false;
		}
		return true;
	}

	public void sendHelp(CommandSender sender) {
		if (!description.isEmpty())
			sender.sendMessage(description);
		sender.sendMessage(ChatColor.GOLD + "Usage" + ChatColor.RESET + ": " + usage.getUsage());
	}

	@Nullable
	public Date getLastUsage(UUID uuid, Event event) {
		if (cooldownStorage == null) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				List<MetadataValue> meta = player.getMetadata("skript_command_last_usage_" + name);
				if (!meta.isEmpty()) {
					return new Date(meta.get(0).asLong());
				}
			}
			return null;
		}
		String variableName = getStorageVariableName(event);
		if (variableName == null)
			return null;
		Object variable = Variables.getVariable(variableName, null, false);
		if (variable instanceof Date) {
			return (Date) variable;
		}
		Skript.warning("Variable {" + variableName + "} was not a date! It is now overridden for the command storage.");
		return null;
	}

	public void setLastUsage(UUID uuid, Event event, @Nullable Date date) {
		if (cooldownStorage != null) {
			String variableName = getStorageVariableName(event);
			if (variableName != null) {
				Variables.setVariable(variableName, date, null, false);
			}
		} else {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				String metaKey = "skript_command_last_usage_" + name;
				if (date == null) {
					player.removeMetadata(metaKey, Skript.getInstance());
				} else {
					player.setMetadata(metaKey, new FixedMetadataValue(Skript.getInstance(), date.getTime()));
				}
			}
		}
	}

	public long getRemainingMilliseconds(UUID uuid, Event event) {
		Date lastUsage = getLastUsage(uuid, event);
		if (lastUsage == null)
			return 0;
		Timespan cooldown = this.cooldown;
		assert cooldown != null;
		long remaining = cooldown.getAs(Timespan.TimePeriod.MILLISECOND) - getElapsedMilliseconds(uuid, event);
		if (remaining < 0)
			remaining = 0;
		return remaining;
	}

	public void setRemainingMilliseconds(UUID uuid, Event event, long milliseconds) {
		Timespan cooldown = this.cooldown;
		assert cooldown != null;
		long cooldownMs = cooldown.getAs(Timespan.TimePeriod.MILLISECOND);
		if (milliseconds > cooldownMs)
			milliseconds = cooldownMs;
		setElapsedMilliSeconds(uuid, event, cooldownMs - milliseconds);
	}

	public long getElapsedMilliseconds(UUID uuid, Event event) {
		Date lastUsage = getLastUsage(uuid, event);
		return lastUsage == null ? 0 : Date.now().getTime() - lastUsage.getTime();
	}

	public void setElapsedMilliSeconds(UUID uuid, Event event, long milliseconds) {
		Date date = Date.now();
		date.subtract(new Timespan(milliseconds));
		setLastUsage(uuid, event, date);
	}

	@Nullable
	private String getStorageVariableName(Event event) {
		if (cooldownStorage == null)
			return null;
		String variableString = cooldownStorage.getSingle(event);
		if (variableString == null)
			return null;
		if (variableString.startsWith("{"))
			variableString = variableString.substring(1);
		if (variableString.endsWith("}"))
			variableString = variableString.substring(0, variableString.length() - 1);
		return variableString;
	}

	@Nullable
	private transient Command overridden = null;
	private transient Map<String, Command> overriddenAliases = new HashMap<>();

	public void register(SimpleCommandMap commandMap, Map<String, Command> knownCommands, @Nullable Set<String> existing) {
		synchronized (commandMap) {
			overriddenAliases.clear();
			overridden = knownCommands.put(name, bukkitCommand);
			if (existing != null)
				existing.remove(name);
			Iterator<String> as = aliases.iterator();
			while (as.hasNext()) {
				final String lowerAlias = as.next().toLowerCase(Locale.ENGLISH);
				if (knownCommands.containsKey(lowerAlias) && (aliases == null || !aliases.contains(lowerAlias))) {
					as.remove();
					continue;
				}
				overriddenAliases.put(lowerAlias, knownCommands.put(lowerAlias, bukkitCommand));
				if (aliases != null)
					aliases.add(lowerAlias);
			}
			bukkitCommand.setAliases(aliases);
			commandMap.register(prefix, bukkitCommand);
		}
	}

	public void unregister(SimpleCommandMap commandMap, Map<String, Command> knownCommands, @Nullable Set<String> existing) {
		synchronized (commandMap) {
			knownCommands.remove(name);
			knownCommands.remove(prefix + ":" + name);
			if (existing != null)
				existing.removeAll(this.aliases);
			for (String alias : aliases) {
				knownCommands.remove(alias);
				knownCommands.remove(prefix + ":" + alias);
			}
			bukkitCommand.unregister(commandMap);
			bukkitCommand.setAliases(this.aliases);
			if (overridden != null) {
				knownCommands.put(name, overridden);
				overridden = null;
			}
			for (Entry<String, Command> e : overriddenAliases.entrySet()) {
				if (e.getValue() == null)
					continue;
				knownCommands.put(e.getKey(), e.getValue());
				if (aliases != null)
					aliases.add(e.getKey());
			}
			overriddenAliases.clear();
		}
	}

	public @Nullable Expression<String> getCooldownMessage() {
		return cooldownMessage;
	}

	public @Nullable Expression<String> getCooldownStorage() {
		return cooldownStorage;
	}

	public @Nullable VariableString getPermissionMessage() {
		return permissionMessage;
	}
	
	public @Nullable List<Argument<?>> getArguments() {
		return arguments;
	}
	
	public @Nullable String getCooldownBypass() {
		return cooldownBypass;
	}
	
	public @Nullable List<String> getAliases() {
		return aliases;
	}
	
	public @Nullable CommandUsage getUsage() {
		return usage;
	}
	
	public @Nullable String getDescription() {
		return description;
	}
	
	public @Nullable Timespan getCooldown() {
		return cooldown;
	}
	
	public @Nullable String getPermission() {
		return permission;
	}

	public PluginCommand getBukkitCommand() {
		return bukkitCommand;
	}

	public @Nullable String getPattern() {
		return pattern;
	}

	public int getExecutableBy() {
		return executableBy;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public String getPrefix() {
		return prefix;
	}

	public Script getScript() {
		return script;
	}

	public String getName() {
		return name;
	}

	public static final class Builder {

		private @Nullable Expression<String> cooldownMessage;
		private @Nullable Expression<String> cooldownStorage;
		private @Nullable VariableString permissionMessage;
		private @Nullable List<Argument<?>> arguments;
		private @Nullable String cooldownBypass;
		private @Nullable List<String> aliases;
		private @Nullable CommandUsage usage;
		private @Nullable String description;
		private @Nullable Timespan cooldown;
		private @Nullable String permission;
		private @Nullable String prefix;

		private int executableBy = BOTH;
		private SectionNode node;
		private String pattern;
		private Script script;
		private String name;

		public Builder() { }

		public Builder(Script script, SectionNode node, String name, String pattern) {
			this.pattern = pattern;
			this.script = script;
			this.name = name;
			this.node = node;
		}

		public Builder permissionMessage(VariableString permissionMessage) {
			this.permissionMessage = permissionMessage;
			return this;
		}

		public Builder cooldownMessage(Expression<String> cooldownMessage) {
			this.cooldownMessage = cooldownMessage;
			return this;
		}

		public Builder cooldownStorage(Expression<String> cooldownStorage) {
			this.cooldownStorage = cooldownStorage;
			return this;
		}

		public Builder arguments(List<Argument<?>> arguments) {
			this.arguments = arguments;
			return this;
		}

		public Builder cooldownBypass(String cooldownBypass) {
			this.cooldownBypass = cooldownBypass;
			return this;
		}

		public Builder description(String description) {
			this.description = description != null ? Utils.replaceEnglishChatStyles(description) : null;
			return this;
		}

		public Builder aliases(List<String> aliases) {
			this.aliases = aliases;
			return this;
		}

		public Builder executableBy(int executableBy) {
			this.executableBy = executableBy;
			return this;
		}

		public Builder permission(String permission) {
			this.permission = permission;
			return this;
		}

		public Builder cooldown(Timespan cooldown) {
			this.cooldown = cooldown;
			return this;
		}

		public Builder usage(CommandUsage usage) {
			this.usage = usage;
			return this;
		}

		public Builder name(@NotNull String name) {
			this.name = name;
			return this;
		}

		public Builder script(@NotNull Script script) {
			this.script = script;
			return this;
		}

		public Builder pattern(@NotNull String pattern) {
			this.pattern = pattern;
			return this;
		}
		
		public Builder prefix(@NotNull String prefix) {
			for (char c : prefix.toCharArray()) {
				if (Character.isWhitespace(c)) {
					Skript.warning("command /" + name + " has a whitespace in its prefix. Defaulting to '" + DEFAULT_PREFIX + "'.");
					prefix = DEFAULT_PREFIX;
					break;
				}
				// char 167 is §
				if (c == 167) {
					Skript.warning("command /" + name + " has a section character in its prefix. Defaulting to '" + DEFAULT_PREFIX + "'.");
					prefix = DEFAULT_PREFIX;
					break;
				}
			}
			this.prefix = prefix;
			return this;
		}

		/**
		 * Builds the ScriptCommand instance.
		 * 
		 * @return a new ScriptCommand
		 */
		public ScriptCommand build() {
			Preconditions.checkNotNull(pattern, "Command pattern cannot be null");
			Preconditions.checkNotNull(script, "Command script cannot be null");
			Preconditions.checkNotNull(name, "Command name cannot be null");
			Preconditions.checkNotNull(node, "Command node cannot be null");
			return new ScriptCommand(this);
		}
	}

}
