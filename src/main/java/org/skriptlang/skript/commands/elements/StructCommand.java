package org.skriptlang.skript.commands.elements;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.command.CommandUsage;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.CommandModule;
import org.skriptlang.skript.commands.api.Argument;
import org.skriptlang.skript.commands.api.CommandCooldown;
import org.skriptlang.skript.commands.api.ScriptCommand;
import org.skriptlang.skript.commands.api.ScriptCommandEvent;
import org.skriptlang.skript.commands.api.ScriptCommandSender;
import org.skriptlang.skript.commands.api.ScriptCommandSender.CommandSenderType;
import org.skriptlang.skript.commands.api.brigadier.ArgumentParser;
import org.skriptlang.skript.commands.bukkit.BukkitScriptCommand;
import org.skriptlang.skript.commands.bukkit.CommandUtils;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.KeyValueEntryData;
import org.skriptlang.skript.lang.entry.util.LiteralEntryData;
import org.skriptlang.skript.lang.entry.util.VariableStringEntryData;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.MatchResult;
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
				.addEntryData(new KeyValueEntryData<List<CommandSenderType>>("executable by", new ArrayList<>(), true) {
					private final Pattern pattern = Pattern.compile("\\s*,\\s*|\\s+(and|or)\\s+");

					@Override
					protected @Nullable List<CommandSenderType> getValue(String value) {
						List<CommandSenderType> executableBy = new ArrayList<>();
						for (String b : pattern.split(value)) {
							if (b.equalsIgnoreCase("console") || b.equalsIgnoreCase("the console")) {
								executableBy.add(CommandSenderType.SERVER);
							} else if (b.equalsIgnoreCase("blocks") || b.equalsIgnoreCase("block")) {
								executableBy.add(CommandSenderType.BLOCK);
							} else if (b.equalsIgnoreCase("entities") || b.equalsIgnoreCase("entity")) {
								executableBy.add(CommandSenderType.ENTITY);
							} else if (b.equalsIgnoreCase("players") || b.equalsIgnoreCase("player")) {
								executableBy.add(CommandSenderType.PLAYER);
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
			"command [/]<^(\\S+)\\s*(.+)?>"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private EntryContainer entryContainer;

	private @Nullable ScriptCommand command;

	// todo: figure out a better way
	// right now the trigger parsing needs access to the arguments, but we can't get them from the command
	// because the command is created after the trigger is parsed
	private @Nullable List<Argument<?>> arguments;

	@SuppressWarnings("NotNullFieldNotInitialized")
	private MatchResult matchResult;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		assert entryContainer != null; // cannot be null for non-simple structures
		this.entryContainer = entryContainer;
			matchResult = parseResult.regexes.get(0);
		return true;
	}

	// todo: this is really bad. this should be like 4 different methods if possible
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

		Matcher commandMatcher = COMMAND_PATTERN.matcher(fullCommand);
		boolean matches = commandMatcher.matches();
		if (!matches) {
			Skript.error("Invalid command structure pattern");
			return false;
		}

		String command = commandMatcher.group(1).toLowerCase();
		ScriptCommand existingCommand = CommandModule.getCommandHandler().getScriptCommand(command);
		if (existingCommand != null && existingCommand.getLabel().equals(command)) {
			Script script = existingCommand.getTrigger().getScript();
			Skript.error("A command with the name /" + existingCommand.getLabel() + " is already defined"
				+ (script != null ? (" in " + script.getConfig().getFileName()) : "")
			);
			getParser().deleteCurrentEvent();
			return false;
		}

		// parse the arguments
		String rawArguments = matchResult.group(2);
		if (rawArguments == null)
			rawArguments = "";

		LiteralArgumentBuilder<ScriptCommandSender<?>> parsedargs = new ArgumentParser<ScriptCommandSender<?>>(command, rawArguments).getRoot();

		Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(rawArguments);
		arguments = new ArrayList<>();
		// arguments are converted into a SkriptPattern
		StringBuilder pattern = new StringBuilder();
		int lastEnd = 0;
		int optionals = 0; // special bracket tracking to avoid counting any within arguments
		while (argumentMatcher.find()) {
			int start = argumentMatcher.start();

			// append all the stuff between the end of the last argument and the beginning of this one
			pattern.append(escape(rawArguments.substring(lastEnd, start)));

			// if this argument is required, we should expect this to be 0
			optionals += StringUtils.count(rawArguments, '[', lastEnd, start);
			optionals -= StringUtils.count(rawArguments, ']', lastEnd, start);

			lastEnd = argumentMatcher.end();

			String rawType = argumentMatcher.group(2);
			ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(rawType);
			NonNullPair<String, Boolean> pluralPair = Utils.getEnglishPlural(rawType);
			if (classInfo == null) { // Attempt parsing the singular version as a backup
				classInfo = Classes.getClassInfoFromUserInput(pluralPair.getFirst());

				if (classInfo == null) { // We tried!
					Skript.error("Unknown type '" + rawType + "'");
					getParser().deleteCurrentEvent();
					return false;
				}
			}
			Parser<?> parser = classInfo.getParser();
			if (parser == null || !parser.canParse(ParseContext.COMMAND)) {
				Skript.error("Can't use " + classInfo + " as argument of a command");
				getParser().deleteCurrentEvent();
				return false;
			}

			// parse argument

			Argument<?> argument = Argument.of(
				argumentMatcher.group(1), classInfo.getC(), optionals > 0, !pluralPair.getSecond(), argumentMatcher.group(3)
			);

			if (argument == null) { // Our argument parsing method should've printed an error
				getParser().deleteCurrentEvent();
				return false;
			}

			arguments.add(argument);

			// append type portion to argument
			pattern.append("%")
					.append(argument.isOptional() ? "-" : "")
					.append(Utils.toEnglishPlural(classInfo.getCodeName(), pluralPair.getSecond()))
					.append("%");
		}

		// append the rest of the user's input
		pattern.append(escape(rawArguments.substring(lastEnd)));

		// ensure brackets have been properly closed
		optionals += StringUtils.count(rawArguments, '[', lastEnd);
		optionals -= StringUtils.count(rawArguments, ']', lastEnd);
		if (optionals != 0) {
			Skript.error("Unclosed brackets within command declaration. Ensure all opening brackets have a respective closing bracket");
			getParser().deleteCurrentEvent();
			return false;
		}

		// obtain and validate the entries

		EntryContainer entryContainer = getEntryContainer();

		String desc = "/" + command + " ";
		desc += StringUtils.replaceAll(pattern, DESCRIPTION_PATTERN, m1 -> {
			assert m1 != null;
			NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + m1.group(1));
			String s1 = p.getFirst();
			return "<" + Classes.getClassInfo(s1).getName().toString(p.getSecond()) + ">";
		});
		desc = unescape(desc).trim();

		VariableString usageMessage = entryContainer.getOptional("usage", VariableString.class, false);
		String defaultUsageMessage = ScriptCommand.M_CORRECT_USAGE + " " + desc;
		CommandUsage usage = new CommandUsage(usageMessage, defaultUsageMessage);

		String description = entryContainer.get("description", String.class, true);
		String prefix = entryContainer.getOptional("prefix", String.class, false);

		String permission = entryContainer.get("permission", String.class, true);
		VariableString permissionMessage = entryContainer.getOptional("permission message", VariableString.class, false);
		if (permissionMessage != null && permission.isEmpty())
			Skript.warning("command /" + command + " has a permission message set, but not a permission");

		List<String> aliases = (List<String>) entryContainer.get("aliases", true);
		List<CommandSenderType> executableBy = (List<CommandSenderType>) entryContainer.get("executable by", true);

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

		CommandCooldown commandCooldown = null;
		if (cooldown != null) {
			commandCooldown = new CommandCooldown(cooldown, cooldownMessage, cooldownBypass, cooldownStorage);
		}


		SectionNode node = entryContainer.getSource();

		if (Skript.debug() || node.debug())
			Skript.debug("command " + desc + ":");

		SectionNode triggerNode = entryContainer.get("trigger", SectionNode.class, false);
		Trigger trigger = new Trigger(getParser().getCurrentScript(), "command /" + command, new SimpleEvent(), ScriptLoader.loadItems(triggerNode));
		trigger.setLineNumber(triggerNode.getLine());

		// construct our command

		if (Skript.logVeryHigh() && !Skript.debug())
			Skript.info("Registered command " + desc);

		getParser().deleteCurrentEvent();

		// todo: make scriptcommand factory method
		this.command = new BukkitScriptCommand(
			command, prefix, description, usage, aliases,
			executableBy, permission, permissionMessage,
			commandCooldown, trigger, arguments,
			PatternCompiler.compile(pattern.toString())
		);
		if (!CommandModule.getCommandHandler().registerCommand(this.command)) {
			// something went wrong, let's hope the register method printed an error
			return false;
		}
		SYNC_COMMANDS.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		attemptCommandSync();
		return true;
	}

	@Override
	public void unload() {
		if (command != null) {
			if (!CommandModule.getCommandHandler().unregisterCommand(command)) {
				throw new SkriptAPIException(
					"An error occurred while attempting to unregister the command '" + command.getLabel() + "'"
				);
			}
			SYNC_COMMANDS.set(true);
		}
	}

	@Override
	public void postUnload() {
		attemptCommandSync();
	}

	private static void attemptCommandSync() {
		if (SYNC_COMMANDS.compareAndSet(true, false)) {
			if (DELAY_COMMAND_SYNCING) {
				// if the plugin is disabled, the server is likely closing and delaying will cause an error.
				if (Bukkit.getPluginManager().isPluginEnabled(Skript.getInstance()))
					Bukkit.getScheduler().runTask(Skript.getInstance(), StructCommand::forceCommandSync);
			} else {
				forceCommandSync();
			}
		}
	}

	private static void forceCommandSync() {
		if (CommandUtils.syncCommands(Bukkit.getServer())) {
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

	public @Nullable List<Argument<?>> getArguments() {
		return this.arguments;
	}

	private static final Pattern ESCAPE = Pattern.compile("[" + Pattern.quote("(|)<>%\\") + "]");
	private static final  Pattern UNESCAPE = Pattern.compile("\\\\[" + Pattern.quote("(|)<>%\\") + "]");

	public static String escape(String s) {
		return ESCAPE.matcher(s).replaceAll("\\\\$0");
	}

	public static String unescape(String s) {
		return UNESCAPE.matcher(s).replaceAll("$0");
	}

}
