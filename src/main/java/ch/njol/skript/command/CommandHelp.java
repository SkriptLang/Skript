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

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RESET;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.SkriptColor;

public class CommandHelp {

	private final static String DEFAULTENTRY = "description";

	private final static ArgsMessage m_invalid_argument = new ArgsMessage("commands.invalid argument");
	private final static Message m_usage = new Message("skript command.usage");

	private final Map<String, Object> arguments = new LinkedHashMap<>();

	private final String actualCommand, actualNode, argsColor;
	private boolean revalidate = true;
	private String command;
	private String langNode;

	@Nullable
	private ArgumentHolder wildcardArg;

	@Nullable
	private SkriptPattern pattern;

	@Nullable
	private Message description;

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

	/**
	 * Add an argument to the CommandHelp.
	 * 
	 * @param argument String that represents the node path to the help info.
	 * @return This CommandHelp for building.
	 */
	public CommandHelp add(String argument) {
		return add(argument, null);
	}

	/**
	 * Add an argument to the CommandHelp.
	 * 
	 * @param argument String that represents the node path to the help info.
	 * @param pattern The pattern to match against user input.
	 * @return This CommandHelp for building.
	 */
	public CommandHelp add(String argument, @Nullable String pattern) {
		ArgumentHolder holder = new ArgumentHolder(argument);
		if (pattern != null)
			holder.pattern = PatternCompiler.compile(pattern);
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

	private Optional<Object> match(String argument) {
		return arguments.entrySet().stream().filter(entry -> {
			Object value = entry.getValue();
			if (value instanceof ArgumentHolder) {
				ArgumentHolder argumentHolder = (ArgumentHolder) value;
				if (argumentHolder.pattern != null)
					return argumentHolder.pattern.match(argument) != null;
			}
			return entry.getKey().equals(argument);
		}).map(Entry::getValue).findFirst();
	}

	public boolean test(CommandSender sender, String[] args) {
		return test(sender, args, 0);
	}

	private boolean test(CommandSender sender, String[] args, int index) {
		if (index >= args.length) {
			showHelp(sender);
			return false;
		}
		Optional<Object> help = match(args[index].toLowerCase(Locale.ENGLISH));
		if (!help.isPresent() && wildcardArg == null) {
			showHelp(sender, m_invalid_argument.toString(argsColor + args[index]));
			return false;
		}
		return !(help.get() instanceof CommandHelp) || ((CommandHelp) help.get()).test(sender, args, index + 1);
	}

	public void showHelp(CommandSender sender) {
		showHelp(sender, m_usage.toString());
	}

	private void showHelp(CommandSender sender, String pre) {
		Skript.message(sender, pre + " " + command + " " + argsColor + "...");
		for (Entry<String, Object> entry : arguments.entrySet()) {
			Skript.message(sender, "  " + argsColor + entry.getKey() + " " + GRAY + "-" + RESET + " " + entry.getValue());
		}
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

		private boolean revalidate = true;
		private final String argument;

		@Nullable
		private SkriptPattern pattern;

		@Nullable
		private Message description; 

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
