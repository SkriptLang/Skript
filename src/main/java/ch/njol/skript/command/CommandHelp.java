package ch.njol.skript.command;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RESET;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.SkriptColor;

public class CommandHelp {

	private final static String DEFAULTENTRY = "description";

	private final static ArgsMessage m_invalid_argument = new ArgsMessage("commands.invalid argument");
	private final static Message m_usage = new Message("skript command.usage");

	private final String actualCommand, actualNode, argsColor;
	private String command;
	private String langNode;

	private boolean revalidate = true;
	@Nullable
	private Message description = null;

	private final Map<String, Object> arguments = new LinkedHashMap<>();

	@Nullable
	private ArgumentHolder wildcardArg = null;

	public CommandHelp(String command, SkriptColor argsColor, String langNode) {
		this(command, argsColor.getFormattedChat(), langNode);
	}

	public CommandHelp(String command, SkriptColor argsColor) {
		this(command, argsColor.getFormattedChat(), command);
	}

	private CommandHelp(String command, String argsColor, String node) {
		this.actualCommand = this.command = command;
		this.actualNode = this.langNode = node;
		this.argsColor = argsColor;
	}

	public CommandHelp add(String argument) {
		ArgumentHolder holder = new ArgumentHolder(argument);
		if (argument.startsWith("<") && argument.endsWith(">")) {
			argument = GRAY + "<" + argsColor + argument.substring(1, argument.length() - 1) + GRAY + ">";
			wildcardArg = holder;
		}
		arguments.put(argument, holder);
		return this;
	}

	public CommandHelp add(CommandHelp help) {
		arguments.put(help.command, help);
		help.onAdd(this);
		return this;
	}

	protected void onAdd(CommandHelp parent) {
		langNode = parent.langNode + "." + actualNode;
		command = parent.command + " " + parent.argsColor + actualCommand;
		revalidate = true;
		for (Entry<String, Object> entry : arguments.entrySet()) {
			if (entry.getValue() instanceof CommandHelp) {
				((CommandHelp) entry.getValue()).onAdd(this);
				continue;
			}
			((ArgumentHolder) entry.getValue()).revalidate = true;
		}
	}

	public boolean test(CommandSender sender, String[] args) {
		return test(sender, args, 0);
	}

	private boolean test(CommandSender sender, String[] args, int index) {
		if (index >= args.length) {
			showHelp(sender);
			return false;
		}
		Object help = arguments.get(args[index].toLowerCase(Locale.ENGLISH));
		if (help == null && wildcardArg == null) {
			showHelp(sender, m_invalid_argument.toString(argsColor + args[index]));
			return false;
		}
		return !(help instanceof CommandHelp) || ((CommandHelp) help).test(sender, args, index + 1);
	}

	public void showHelp(CommandSender sender) {
		showHelp(sender, m_usage.toString());
	}

	private void showHelp(CommandSender sender, String pre) {
		Skript.message(sender, pre + " " + command + " " + argsColor + "...");
		for (Entry<String, Object> entry : arguments.entrySet())
			Skript.message(sender, "  " + argsColor + entry.getKey() + " " + GRAY + "-" + RESET + " " + entry.getValue());
	}

	@Override
	public String toString() {
		if (revalidate) {
			// We don't want to create a new Message object each time toString is called
			description = new Message(langNode + "." + DEFAULTENTRY);
			revalidate = false;
		}
		return "" + description;
	}

	private class ArgumentHolder {

		private final String argument;
		private boolean revalidate = true;
		@Nullable
		private Message description = null; 

		private ArgumentHolder(String argument) {
			this.argument = argument;
		}

		@Override
		public String toString() {
			if (revalidate) {
				description = new Message(langNode + "." + argument);
				revalidate = false;
			}
			return "" + description;
		}

	}

}
