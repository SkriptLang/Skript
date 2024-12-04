package org.skriptlang.skript.commands.api;

import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandUsage;
import ch.njol.skript.command.Commands;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.api.ScriptCommandSender.CommandSenderType;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public abstract class ScriptCommand {

	// TODO figure out if used
	public static final ArgsMessage M_TOO_MANY_ARGUMENTS = new ArgsMessage("commands.too many arguments");
	public static final Message M_INTERNAL_ERROR = new Message("commands.internal error");
	public static final Message M_CORRECT_USAGE = new Message("commands.correct usage");

	private static final String DEFAULT_NAMESPACE = "skript";

	private final String namespace;
	private final String label;
	private final String description;
	private final CommandUsage usage;
	private final List<String> aliases;
	private final @Nullable String permission;
	private final VariableString permissionMessage;
	private final List<CommandSenderType> executableBy;
	private final @Nullable CommandCooldown cooldown;
	private final Trigger trigger;
	private final List<Argument<?>> arguments;
	private final SkriptPattern pattern;

	public ScriptCommand(
		String label, @Nullable String namespace, String description, CommandUsage usage, List<String> aliases,
		List<CommandSenderType> executableBy, @Nullable String permission, @Nullable VariableString permissionMessage,
		@Nullable CommandCooldown cooldown, Trigger trigger, List<Argument<?>> arguments, SkriptPattern pattern
	) {
		this.label = label.toLowerCase(Locale.ENGLISH);
		this.description = Utils.replaceEnglishChatStyles(description);
		this.usage = usage;

		if (namespace != null) {
			for (char c : namespace.toCharArray()) {
				if (Character.isWhitespace(c)) {
					Skript.warning("command /" + label + " has a whitespace in its prefix. Defaulting to '" + ScriptCommand.DEFAULT_NAMESPACE + "'.");
					namespace = DEFAULT_NAMESPACE;
					break;
				}
				// char 167 is §
				if (c == 167) {
					Skript.warning("command /" + label + " has a section character in its prefix. Defaulting to '" + ScriptCommand.DEFAULT_NAMESPACE + "'.");
					namespace = DEFAULT_NAMESPACE;
					break;
				}
			}
		} else {
			namespace = DEFAULT_NAMESPACE;
		}
		this.namespace = namespace;

		aliases.removeIf(label::equalsIgnoreCase);
		this.aliases = aliases;

		this.executableBy = executableBy;
		this.permission = permission;

		VariableString possiblePermissionMessage = permissionMessage;
		if (possiblePermissionMessage == null) {
			VariableString defaultMessage = VariableString.newInstance(Language.get("commands.no permission message"));
			assert defaultMessage != null;
			possiblePermissionMessage = defaultMessage;
		}
		this.permissionMessage = possiblePermissionMessage;

		this.cooldown = cooldown;

		this.trigger = trigger;
		this.arguments = arguments;
		this.pattern = pattern;
	}

	/**
	 * Attempts to execute this command with the given sender, label and raw arguments.
	 * This method should call {@link #execute_i(ScriptCommandSender, String, String[])} to execute the command,
	 * though implementations may choose to handle those responsibilities themselves
	 * if {@link #execute_i(ScriptCommandSender, String, String[])} does not suffice.
	 * <br>
	 * Any implementation of this method should check if the sender has the permission to execute this command,
	 * if the sender is of the right type to execute this command, and if the sender is on cooldown for this command.
	 *
	 * @param sender the {@link ScriptCommandSender} that executed the command
	 * @param label the label used to execute the command
	 * @param args the arguments used to execute the command
	 */
	public abstract void execute(ScriptCommandSender sender, String label, String[] args);

	/**
	 * Attempts to execute this command with the given sender, label and raw arguments.
	 * If the command fails to execute, the method returns {@code false} and send the appropriate error message to the sender.
	 *
	 * @param sender the {@link ScriptCommandSender} that executed the command
	 * @param label the label used to execute the command
	 * @param args the arguments used to execute the command
	 */
	protected void execute_i(ScriptCommandSender sender, String label, String[] args) {

		// check if the sender is of the right type to execute this command
		if (!checkExecutable(sender)) {
			sender.sendMessage(sender.getType().getErrorMessage());
			return;
		}

		// create the event for this command
		ScriptCommandEvent event = new ScriptCommandEvent(sender, this, label, args);

		// check if the sender has the permission to execute this command
		if (!checkPermission(sender)) {
			sender.sendMessage(permissionMessage.getMessageComponents(event));
			return;
		}

		// check if the sender is on cooldown for this command
		if (!checkCooldown(sender, event)) {
			assert cooldown != null;
			sender.sendMessage(cooldown.getCooldownMessage().getMessageComponents(event));
			return;
		}

		// argument parsing
		String joinedArgs = StringUtils.join(args, " ");
		if (!parseArguments(joinedArgs, event)) {
			sender.sendMessage(usage.getUsage(event));
			return;
		}

		// set variables for argument
		setArgumentVariables(event);

		// execute the command
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + label + " " + String.join(" ", args));
		final long startTrigger = System.nanoTime();

		if (!trigger.execute(event))
			sender.sendMessage(Commands.m_internal_error.toString());

		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + label + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");

		// put the sender on cooldown
		if (cooldown != null && !event.isCooldownCancelled())
			cooldown.applyCooldown(sender, event);
	}

	/**
	 * Checks whether the {@link ScriptCommandSender} is of the correct type to execute this command.
	 *
	 * @param sender the {@link ScriptCommandSender} to check
	 * @return {@code true} if the {@link ScriptCommandSender} is of the correct type to execute this command, {@code false} otherwise
	 */
	public boolean checkExecutable(ScriptCommandSender sender) {
		if (executableBy.isEmpty())
			return true;
		for (CommandSenderType type : executableBy) {
			if (type == sender.getType())
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the {@link ScriptCommandSender} has the permission to execute this command.
	 *
	 * @param sender the {@link ScriptCommandSender} to check
	 * @return {@code true} if the {@link ScriptCommandSender} has the permission to execute this command, {@code false} otherwise
	 */
	public boolean checkPermission(ScriptCommandSender sender) {
		return permission == null || sender.hasPermission(permission);
	}

	/**
	 * Checks whether the {@link ScriptCommandSender} is on cooldown for this command.
	 *
	 * @param sender the {@link ScriptCommandSender} to check
	 * @param event the {@link Event} to use to evaluate the cooldown variables, if needed
	 * @return {@code true} if the {@link ScriptCommandSender} is not on cooldown, {@code false} otherwise
	 */
	public boolean checkCooldown(ScriptCommandSender sender, Event event) {
		if (cooldown == null)
			return true;
		UUID uuid = sender.getUniqueID();
		return uuid == null || cooldown.isOnCooldown(sender, event);
	}

	/**
	 * Parses the raw string of arguments into the arguments of this command.
	 *
	 * @param arguments the raw string of arguments
	 * @param event the event for this execution of the command
	 * @return {@code true} if the arguments were parsed successfully, {@code false} otherwise
	 */
	private boolean parseArguments(String arguments, ScriptCommandEvent event){
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {

			MatchResult matchResult = pattern.match(arguments, SkriptParser.PARSE_LITERALS, ParseContext.COMMAND);
			if (matchResult == null) {
				return false;
			}

			SkriptParser.ParseResult parseResult = matchResult.toParseResult();
			assert this.arguments.size() == parseResult.exprs.length;
			for (int i = 0; i < parseResult.exprs.length; i++) {
				if (parseResult.exprs[i] != null) {
					//noinspection unchecked - generics moment :)
					((Argument<Object>) this.arguments.get(i)).setValues(event, parseResult.exprs[i].getArray(event));
				}
			}

			log.clearError();
		}
		return true;
	}

	/**
	 * Sets all the relevant local variables for any named arguments this command has.
	 *
	 * @param event the event for this execution of the command
	 */
	private void setArgumentVariables(ScriptCommandEvent event) {
		for (Argument<?> argument : arguments) {
			// if the argument is named, set variables too
			String name = argument.getName();
			if (name != null) {
				Object[] values = argument.getValues(event);
				if (values == null)
					continue;
				if (argument.isSingle()) {
					if (values.length > 0)
						Variables.setVariable(name, values[0], event, true);
				} else {
					for (int i = 0; i < values.length; i++) {
						Variables.setVariable(name + Variable.SEPARATOR + (i + 1), values[i], event, true);
					}
				}
			}
		}
	}


	public String getNamespace() {
		return namespace;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public CommandUsage getUsage() {
		return usage;
	}

	public List<String> getAliases() {
		return aliases;
	}

	@Nullable
	public String getPermission() {
		return permission;
	}

	public VariableString getPermissionMessage() {
		return permissionMessage;
	}

	public List<CommandSenderType> getExecutableBy() {
		return executableBy;
	}

	@Nullable
	public CommandCooldown getCooldown() {
		return cooldown;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public List<Argument<?>> getArguments() {
		return arguments;
	}

	public SkriptPattern getPattern() {
		return pattern;
	}
}
