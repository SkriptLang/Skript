package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.CommandReloader;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.CommandUsage;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.LiteralEntryData;
import org.skriptlang.skript.lang.entry.util.VariableStringEntryData;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Command")
@Description("Used for registering custom commands.")
@Examples({
	"command /broadcast <string>:",
	"\tusage: A command for broadcasting a message to all players.",
	"\tpermission: skript.command.broadcast",
	"\tpermission message: You don't have permission to broadcast messages",
	"\taliases: /bc",
	"\texecutable by: players and console",
	"\tcooldown: 15 seconds",
	"\tcooldown message: You last broadcast a message %elapsed time% ago. You can broadcast another message in %remaining time%.",
	"\tcooldown bypass: skript.command.broadcast.admin",
	"\tcooldown storage: {cooldown::%player%}",
	"\ttrigger:",
	"\t\tbroadcast the argument"
})
@Since("1.0")
public class StructCommand extends Structure {

	// Paper versions with the new command system need a delay before syncing commands or a CME will occur.
	private static final boolean DELAY_COMMAND_SYNCING = Skript.classExists("io.papermc.paper.command.brigadier.Commands");

	public static final Priority PRIORITY = new Priority(500);

	private static final Pattern COMMAND_PATTERN = Pattern.compile("(?i)^command\\s+/?(\\S+)\\s*(\\s+(.+))?$");
	private static final Pattern ARGUMENT_PATTERN = Pattern.compile("<\\s*(?:([^>]+?)\\s*:\\s*)?(.+?)\\s*(?:=\\s*(" + SkriptParser.WILDCARD + "))?\\s*>");
	private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("(?<!\\\\)%-?(.+?)%");

	private static final AtomicBoolean SYNC_COMMANDS = new AtomicBoolean();

	static {
		Skript.registerStructure(
			StructCommand.class,
			EntryValidator.builder()
				.addEntryData(new VariableStringEntryData("usage", null, true))
				.addEntry("description", "", true)
				.addEntry("prefix", null, true)
				.addEntry("permission", "", true)
				.addEntryData(new VariableStringEntryData("permission message", null, true))
				.addEntryData(new KeyValueEntryData<List<String>>("aliases", new ArrayList<>(), true) {
					private final Pattern pattern = Pattern.compile("\\s*,\\s*/?");

					@Override
					protected List<String> getValue(String value) {
						List<String> aliases = new ArrayList<>(Arrays.asList(pattern.split(value)));
						if (aliases.get(0).startsWith("/")) {
							aliases.set(0, aliases.get(0).substring(1));
						} else if (aliases.get(0).isEmpty()) {
							aliases = new ArrayList<>(0);
						}
						return aliases;
					}
				})
				.addEntryData(new KeyValueEntryData<Integer>("executable by", ScriptCommand.CONSOLE | ScriptCommand.PLAYERS, true) {
					private final Pattern pattern = Pattern.compile("\\s*,\\s*|\\s+(and|or)\\s+");

					@Override
					@Nullable
					protected Integer getValue(String value) {
						int executableBy = 0;
						for (String b : pattern.split(value)) {
							if (b.equalsIgnoreCase("console") || b.equalsIgnoreCase("the console")) {
								executableBy |= ScriptCommand.CONSOLE;
							} else if (b.equalsIgnoreCase("players") || b.equalsIgnoreCase("player")) {
								executableBy |= ScriptCommand.PLAYERS;
							} else {
								return null;
							}
						}
						return executableBy;
					}
				})
				.addEntryData(new LiteralEntryData<>("cooldown", null, true, Timespan.class))
				.addEntryData(new VariableStringEntryData("cooldown message", null, true))
				.addEntry("cooldown bypass", null, true)
				.addEntryData(new VariableStringEntryData("cooldown storage", null, true, StringMode.VARIABLE_NAME))
				.addSection("trigger", false)
				.unexpectedEntryMessage(key ->
					"Unexpected entry '" + key + "'. Check that it's spelled correctly, and ensure that you have put all code into a trigger."
				)
				.build(),
			"command <.+>"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private EntryContainer entryContainer;

	@Nullable
	private ScriptCommand scriptCommand;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		assert entryContainer != null; // cannot be null for non-simple structures
		this.entryContainer = entryContainer;
		return true;
	}

	@Override
	public boolean load() {
		getParser().setCurrentEvent("command", ScriptCommandEvent.class);

		String fullCommand = entryContainer.getSource().getKey();
		assert fullCommand != null;
		fullCommand = ScriptLoader.replaceOptions(fullCommand);

		int level = 0;
		for (int i = 0; i < fullCommand.length(); i++) {
			if (fullCommand.charAt(i) == '[') {
				level++;
			} else if (fullCommand.charAt(i) == ']') {
				if (level == 0) {
					Skript.error("Invalid placement of [optional brackets]");
					getParser().deleteCurrentEvent();
					return false;
				}
				level--;
			}
		}
		if (level > 0) {
			Skript.error("Invalid amount of [optional brackets]");
			getParser().deleteCurrentEvent();
			return false;
		}

		Matcher matcher = COMMAND_PATTERN.matcher(fullCommand);
		boolean matches = matcher.matches();
		if (!matches) {
			Skript.error("Invalid command structure pattern");
			return false;
		}

		String command = matcher.group(1).toLowerCase();
		ScriptCommand existingCommand = Commands.getScriptCommand(command);
		if (existingCommand != null && existingCommand.getLabel().equals(command)) {
			Script script = existingCommand.getScript();
			Skript.error("A command with the name /" + existingCommand.getName() + " is already defined"
				+ (script != null ? (" in " + script.getConfig().getFileName()) : "")
			);
			getParser().deleteCurrentEvent();
			return false;
		}

		String arguments = matcher.group(3) == null ? "" : matcher.group(3);
		StringBuilder pattern = new StringBuilder();

		List<Argument<?>> currentArguments = Commands.currentArguments = new ArrayList<>(); //Mirre
		matcher = ARGUMENT_PATTERN.matcher(arguments);
		int lastEnd = 0;
		int optionals = 0;
		for (int i = 0; matcher.find(); i++) {
			pattern.append(Commands.escape(arguments.substring(lastEnd, matcher.start())));
			optionals += StringUtils.count(arguments, '[', lastEnd, matcher.start());
			optionals -= StringUtils.count(arguments, ']', lastEnd, matcher.start());

			lastEnd = matcher.end();

			ClassInfo<?> c;
			c = Classes.getClassInfoFromUserInput(matcher.group(2));
			NonNullPair<String, Boolean> p = Utils.getEnglishPlural(matcher.group(2));
			if (c == null)
				c = Classes.getClassInfoFromUserInput(p.getFirst());
			if (c == null) {
				Skript.error("Unknown type '" + matcher.group(2) + "'");
				getParser().deleteCurrentEvent();
				return false;
			}
			Parser<?> parser = c.getParser();
			if (parser == null || !parser.canParse(ParseContext.COMMAND)) {
				Skript.error("Can't use " + c + " as argument of a command");
				getParser().deleteCurrentEvent();
				return false;
			}

			Argument<?> arg = Argument.newInstance(matcher.group(1), c, matcher.group(3), i, !p.getSecond(), optionals > 0);
			if (arg == null) {
				getParser().deleteCurrentEvent();
				return false;
			}
			currentArguments.add(arg);

			if (arg.isOptional() && optionals == 0) {
				pattern.append('[');
				optionals++;
			}
			pattern.append("%").append(arg.isOptional() ? "-" : "").append(Utils.toEnglishPlural(c.getCodeName(), p.getSecond())).append("%");
		}

		pattern.append(Commands.escape("" + arguments.substring(lastEnd)));
		optionals += StringUtils.count(arguments, '[', lastEnd);
		optionals -= StringUtils.count(arguments, ']', lastEnd);
		for (int i = 0; i < optionals; i++)
			pattern.append(']');

		String desc = "/" + command + " ";
		desc += StringUtils.replaceAll(pattern, DESCRIPTION_PATTERN, m1 -> {
			assert m1 != null;
			NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + m1.group(1));
			String s1 = p.getFirst();
			return "<" + Classes.getClassInfo(s1).getName().toString(p.getSecond()) + ">";
		});
		desc = Commands.unescape(desc).trim();

		VariableString usageMessage = entryContainer.getOptional("usage", VariableString.class, false);
		String defaultUsageMessage = Commands.m_correct_usage + " " + desc;
		CommandUsage usage = new CommandUsage(usageMessage, defaultUsageMessage);

		String description = entryContainer.get("description", String.class, true);
		String prefix = entryContainer.getOptional("prefix", String.class, false);

		String permission = entryContainer.get("permission", String.class, true);
		VariableString permissionMessage = entryContainer.getOptional("permission message", VariableString.class, false);
		if (permissionMessage != null && permission.isEmpty())
			Skript.warning("command /" + command + " has a permission message set, but not a permission");

		List<String> aliases = entryContainer.get("aliases", List.class,true);
		int executableBy = entryContainer.get("executable by", Integer.class, true);

		Timespan cooldown = entryContainer.getOptional("cooldown", Timespan.class, false);
		VariableString cooldownMessage = entryContainer.getOptional("cooldown message", VariableString.class, false);
		if (cooldownMessage != null && cooldown == null)
			Skript.warning("command /" + command + " has a cooldown message set, but not a cooldown");
		String cooldownBypass = entryContainer.getOptional("cooldown bypass", String.class, false);
		if (cooldownBypass == null) {
			cooldownBypass = "";
		} else if (cooldownBypass.isEmpty() && cooldown == null) {
			Skript.warning("command /" + command + " has a cooldown bypass set, but not a cooldown");
		}
		VariableString cooldownStorage = entryContainer.getOptional("cooldown storage", VariableString.class, false);
		if (cooldownStorage != null && cooldown == null)
			Skript.warning("command /" + command + " has a cooldown storage set, but not a cooldown");

		SectionNode node = entryContainer.getSource();

		if (Skript.debug() || node.debug())
			Skript.debug("command " + desc + ":");

		Commands.currentArguments = currentArguments;
		try {
			scriptCommand = new ScriptCommand(getParser().getCurrentScript(), command, pattern.toString(), currentArguments, description, prefix,
				usage, aliases, permission, permissionMessage, cooldown, cooldownMessage, cooldownBypass, cooldownStorage,
				executableBy, entryContainer.get("trigger", SectionNode.class, false));
		} finally {
			Commands.currentArguments = null;
		}

		if (Skript.logVeryHigh() && !Skript.debug())
			Skript.info("Registered command " + desc);

		getParser().deleteCurrentEvent();

		Commands.registerCommand(scriptCommand);
		SYNC_COMMANDS.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		scheduleCommandSync();
		return true;
	}

	@Override
	public void unload() {
		assert scriptCommand != null; // This method should never be called if one of the loading methods fail
		Commands.unregisterCommand(scriptCommand);
		SYNC_COMMANDS.set(true);
	}

	@Override
	public void postUnload() {
		scheduleCommandSync();
	}

	private void scheduleCommandSync() {
		if (SYNC_COMMANDS.get()) {
			SYNC_COMMANDS.set(false);
			if (DELAY_COMMAND_SYNCING) {
				// if the plugin is disabled, the server is likely closing and delaying will cause an error.
				if (Bukkit.getPluginManager().isPluginEnabled(Skript.getInstance()))
					Bukkit.getScheduler().runTask(Skript.getInstance(), this::forceCommandSync);
			} else {
				forceCommandSync();
			}
		}
	}

	private void forceCommandSync() {
		if (CommandReloader.syncCommands(Bukkit.getServer())) {
			Skript.debug("Commands synced to clients");
		} else {
			Skript.debug("Commands changed but not synced to clients (normal on 1.12 and older)");
		}
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "command";
	}

}
