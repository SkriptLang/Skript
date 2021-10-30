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
package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Command Info")
@Description("Get information about a command.")
@Examples({"main name of command \"skript\"",
	"description of command \"help\"",
	"label of command \"pl\"",
	"usage of command \"help\"",
	"aliases of command \"bukkit:help\"",
	"permission of command \"/op\"",
	"command \"op\"'s permission message",
	"command \"sk\"'s plugin owner"})
@Since("2.6")
public class ExprCommandInfo extends SimpleExpression<String> {

	private enum InfoType {
		NAME,
		DESCRIPTION,
		LABEL,
		USAGE,
		ALIASES,
		PERMISSION,
		PERMISSION_MESSAGE,
		PLUGIN,
	}

	static {
		Skript.registerExpression(ExprCommandInfo.class, String.class, ExpressionType.SIMPLE,
			"[the] main command [label] of command %strings%", "command %strings%'[s] main command [name]",
			"[the] description of command %strings%", "command %strings%'[s] description",
			"[the] label of command %strings%", "command %strings%'[s] label",
			"[the] usage of command %strings%", "command %strings%'[s] usage",
			"[(all|the|all [of] the)] aliases of command %strings%", "command %strings%'[s] aliases",
			"[the] permission of command %strings%", "command %strings%'[s] permission",
			"[the] permission message of command %strings%", "command %strings%'[s] permission message",
			"[the] plugin [owner] of command %strings%", "command %strings%'[s] plugin [owner]");
	}

	@SuppressWarnings("null")
	InfoType type;
	@SuppressWarnings("null")
	Expression<String> commandName;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		commandName = (Expression<String>) exprs[0];
		type = InfoType.values()[Math.floorDiv(matchedPattern, 2)];
		return true;
	}

	@Nullable
	@Override
	@SuppressWarnings("null")
	protected String[] get(Event e) {
		CommandMap map = Commands.getCommandMap();
		Command[] commands = this.commandName.stream(e).map(map::getCommand).filter(Objects::nonNull).toArray(Command[]::new);
		ArrayList<String> result = new ArrayList<>();
		switch (type) {
			case NAME:
				for (Command command : commands)
					result.add(command.getName());
				break;
			case DESCRIPTION:
				for (Command command : commands)
					result.add(command.getDescription());
				break;
			case LABEL:
				for (Command command : commands)
					result.add(command.getLabel());
				break;
			case USAGE:
				for (Command command : commands)
					result.add(command.getUsage());
				break;
			case ALIASES:
				for (Command command : commands)
					result.addAll(command.getAliases());
				break;
			case PERMISSION:
				for (Command command : commands)
					result.add(command.getPermission());
				break;
			case PERMISSION_MESSAGE:
				for (Command command : commands)
					result.add(command.getPermissionMessage());
				break;
			case PLUGIN:
				for (Command command : commands) {
					if (command instanceof PluginCommand) {
						result.add(((PluginCommand) command).getPlugin().getName());
					} else if (command instanceof BukkitCommand) {
						result.add("Bukkit");
					} else if (command.getClass().getPackage().getName().startsWith("org.spigot")) {
						result.add("Spigot");
					} else if (command.getClass().getPackage().getName().startsWith("com.destroystokyo.paper")) {
						result.add("Paper");
					}
				}
				break;
		}
		return result.toArray(new String[0]);
	}

	@Override
	public boolean isSingle() {
		return type == InfoType.ALIASES || commandName.isSingle();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the " + type.name().toLowerCase().replace("_", " ") + " of command " + commandName.toString(e, debug);
	}
}
