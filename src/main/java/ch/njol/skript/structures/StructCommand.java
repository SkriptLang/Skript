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
package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader.ScriptInfo;
import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class StructCommand extends Structure {

	static {
		Skript.registerStructure(StructCommand.class, "command <.+>");
		ParserInstance.registerData(CommandData.class, CommandData::new);
	}

	@Nullable
	private ScriptCommand command;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode node) {
		ScriptInfo scriptInfo = getParser().getScriptInfo();

		getParser().setCurrentEvent("command", CommandEvent.class);

		command = Commands.loadCommand(node, false);
		if (command != null) {
			scriptInfo.commandNames.add(command.getName()); // For tab completion
			scriptInfo.commands++;
		}

		getParser().deleteCurrentEvent();

		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "command";
	}

	public static class CommandData extends ParserInstance.Data {
		public CommandData(ParserInstance parserInstance) {
			super(parserInstance);
		}

		@Override
		public void onCurrentScriptChange(@Nullable Config currentScript) {
			Runnable runnable = () -> {
				for (Structure structure : getParser().getLoadedStructures()) {
					if (structure instanceof StructCommand) {
						ScriptCommand command = ((StructCommand) structure).command;
						if (command != null)
							Commands.registerCommand(command);
					}
				}
			};

			if (Bukkit.isPrimaryThread())
				runnable.run();
			else
				Bukkit.getScheduler().runTask(Skript.getInstance(), runnable);
		}
	}

}
