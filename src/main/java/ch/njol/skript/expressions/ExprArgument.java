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

import java.util.List;
import java.util.regex.MatchResult;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

@Name("Argument")
@Description({
	"Usable in script commands and command events. Holds the value of an argument given to the command, " +
	"e.g. if the command \"/tell &lt;player&gt; &lt;text&gt;\" is used like \"/tell Njol Hello Njol!\" argument 1 is the player named \"Njol\" and argument 2 is \"Hello Njol!\".",
	"One can also use the type of the argument instead of its index to address the argument, e.g. in the above example 'player-argument' is the same as 'argument 1'.",
	"Please note that specifying the argument type is only supported in script commands."
})
@Examples({
	"give the item-argument to the player-argument",
	"damage the player-argument by the number-argument",
	"give a diamond pickaxe to the argument",
	"add argument 1 to argument 2",
	"heal the last argument"
})
@Since("1.0, 2.7 (support for command events)")
public class ExprArgument extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprArgument.class, Object.class, ExpressionType.SIMPLE,
				"[the] last arg[ument]", // LAST
				"[the] arg[ument](-| )<(\\d+)>", // ORDINAL
				"[the] <(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th> arg[ument][s]", // ORDINAL
				"[(all [[of] the]|the)] arg[ument][(1:s)]", // SINGLE OR ALL
				"[the] %*classinfo%( |-)arg[ument][( |-)<\\d+>]", // CLASSINFO
				"[the] arg[ument]( |-)%*classinfo%[( |-)<\\d+>]" // CLASSINFO
		);
	}

	private static final int LAST = 0, ORDINAL = 1, SINGLE = 2, ALL = 3, CLASSINFO = 4;
	private int what;

	@Nullable
	private Argument<?> argument;

	private int ordinal = -1; // Available in ORDINAL and sometimes CLASSINFO
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean scriptCommand = getParser().isCurrentEvent(ScriptCommandEvent.class);
		if (!scriptCommand && !getParser().isCurrentEvent(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class)) {
			Skript.error("The 'argument' expression can only be used in a script command or command event");
			return false;
		}

		switch (matchedPattern) {
			case 0:
				what = LAST;
				break;
			case 1:
			case 2:
				what = ORDINAL;
				break;
			case 3:
				what = parseResult.mark == 1 ? ALL : SINGLE;
				break;
			case 4:
			case 5:
				what = CLASSINFO;
				break;
			default:
				assert false;
		}

		if (!scriptCommand && what == CLASSINFO) {
			Skript.error("Command event arguments are strings, meaning type specification is useless");
			return false;
		}

		List<Argument<?>> currentArguments = Commands.currentArguments;
		if (scriptCommand && (currentArguments == null || currentArguments.isEmpty())) {
			Skript.error("This command doesn't have any arguments", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}

		if (what == ORDINAL) {
			// Figure out in which format (1st, 2nd, 3rd, Nth) argument was given in
			MatchResult regex = parseResult.regexes.get(0);
			String argMatch = null;
			for (int i = 1; i <= 4; i++) {
				argMatch = regex.group(i);
				if (argMatch != null) {
					break; // Found format
				}
			}
			assert argMatch != null;
			ordinal = Utils.parseInt(argMatch);
			if (scriptCommand && ordinal > currentArguments.size()) { // Only check if it's a script command as we know nothing of command event arguments
				Skript.error("This command doesn't have a " + StringUtils.fancyOrderNumber(ordinal) + " argument", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}

		if (scriptCommand) { // Handle before execution
			switch (what) {
				case LAST:
					argument = currentArguments.get(currentArguments.size() - 1);
					break;
				case ORDINAL:
					argument = currentArguments.get(ordinal - 1);
					break;
				case SINGLE:
					if (currentArguments.size() == 1) {
						argument = currentArguments.get(0);
					} else {
						Skript.error("This command has multiple arguments, meaning it is not possible to get the 'argument'. Use 'argument 1', 'argument 2', etc. instead", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
					break;
				case ALL:
					Skript.error("'arguments' cannot be used for script commands. Use 'argument 1', 'argument 2', etc. instead", ErrorQuality.SEMANTIC_ERROR);
					return false;
				case CLASSINFO:
					ClassInfo<?> c = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
					if (parseResult.regexes.size() > 0) {
						ordinal = Utils.parseInt(parseResult.regexes.get(0).group());
						if (ordinal > currentArguments.size()) {
							Skript.error("This command doesn't have a " + StringUtils.fancyOrderNumber(ordinal) + " " + c + " argument", ErrorQuality.SEMANTIC_ERROR);
							return false;
						}
					}

					Argument<?> arg = null;
					int argAmount = 0;
					for (Argument<?> a : currentArguments) {
						if (!c.getC().isAssignableFrom(a.getType())) // This argument is not of the required type
							continue;

						if (ordinal == -1 && argAmount == 2) { // The user said '<type> argument' without specifying which, and multiple arguments for the type exist
							Skript.error("There are multiple " + c + " arguments in this command", ErrorQuality.SEMANTIC_ERROR);
							return false;
						}

						arg = a;

						argAmount++;
						if (argAmount == ordinal) { // There is argNum argument for the required type (ex: "string argument 2" would exist)
							break;
						}
					}

					if (argAmount == 0) {
						Skript.error("There is no " + c + " argument in this command", ErrorQuality.SEMANTIC_ERROR);
						return false;
					} else if (ordinal > argAmount) { // The user wanted an argument number that didn't exist for the given type
						if (argAmount == 1) {
							Skript.error("There is only one " + c + " argument in this command", ErrorQuality.SEMANTIC_ERROR);
						} else {
							Skript.error("There are only " + argAmount + " " + c + " arguments in this command", ErrorQuality.SEMANTIC_ERROR);
						}
						return false;
					}

					// 'arg' will never be null here
					argument = arg;
					break;
				default:
					assert false : what;
					return false;
			}
		}

		return true;
	}
	
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		if (argument != null) {
			return argument.getCurrent(e);
		}

		String fullCommand;
		if (e instanceof PlayerCommandPreprocessEvent) {
			fullCommand = ((PlayerCommandPreprocessEvent) e).getMessage().substring(1).trim();
		} else if (e instanceof ServerCommandEvent) { // It's a ServerCommandEvent then
			fullCommand = ((ServerCommandEvent) e).getCommand().trim();
		} else {
			return new Object[0];
		}

		String[] arguments;
		int firstSpace = fullCommand.indexOf(' ');
		if (firstSpace != -1) {
			fullCommand = fullCommand.substring(firstSpace + 1);
			arguments = fullCommand.split(" ");
		} else { // No arguments, just the command
			return new String[0];
		}

		switch (what) {
			case LAST:
				if (arguments.length > 0)
					return new String[]{arguments[arguments.length - 1]};
				break;
			case ORDINAL:
				if (arguments.length >= ordinal)
					return new String[]{arguments[ordinal - 1]};
				break;
			case SINGLE:
				if (arguments.length == 1)
					return new String[]{arguments[arguments.length - 1]};
				break;
			case ALL:
				return arguments;
		}

		return new Object[0];
	}

	@Override
	public boolean isSingle() {
		return argument != null ? argument.isSingle() : what != ALL;
	}
	
	@Override
	public Class<?> getReturnType() {
		return argument != null ? argument.getType() : String.class;
	}

	@Override
	public boolean isLoopOf(String s) {
		return s.equalsIgnoreCase("argument");
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		switch (what) {
			case LAST:
				return "the last argument";
			case ORDINAL:
				return "the " + StringUtils.fancyOrderNumber(ordinal) + " argument";
			case SINGLE:
				return "the argument";
			case ALL:
				return "the arguments";
			case CLASSINFO:
				assert argument != null;
				ClassInfo<?> ci = Classes.getExactClassInfo(argument.getType());
				assert ci != null; // If it was null, that would be very bad
				return "the " + ci + " argument " + (ordinal != -1 ? ordinal : ""); // Add the argument number if the user gave it before
			default:
				return "argument";
		}
	}
	
}
