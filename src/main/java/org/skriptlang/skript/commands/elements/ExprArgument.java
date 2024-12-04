package org.skriptlang.skript.commands.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.commands.api.Argument;
import org.skriptlang.skript.commands.api.ScriptCommandEvent;

import java.util.List;
import java.util.regex.MatchResult;

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
				"[all [[of] the]|the] arg[ument][all:s]", // SINGLE OR ALL
				"[the] %*classinfo%( |-)arg[ument][( |-)<\\d+>]", // CLASSINFO
				"[the] arg[ument]( |-)%*classinfo%[( |-)<\\d+>]" // CLASSINFO
		);
	}

	private enum ArgumentType {
		LAST, ORDINAL, SINGLE, ALL, CLASSINFO
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private ArgumentType argumentType;

	private @Nullable Argument<?> argument;

	private int ordinal = -1; // Available in ORDINAL and sometimes CLASSINFO
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ParserInstance parser = getParser();

		boolean scriptCommand = parser.isCurrentStructure(StructCommand.class);
		if (!scriptCommand && !parser.isCurrentEvent(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class)) {
			Skript.error("The 'argument' expression can only be used in a script command or command event");
			return false;
		}

		switch (matchedPattern) {
			case 0:
				argumentType = ArgumentType.LAST;
				break;
			case 1:
			case 2:
				argumentType = ArgumentType.ORDINAL;
				break;
			case 3:
				argumentType = parseResult.hasTag("all") ? ArgumentType.ALL : ArgumentType.SINGLE;
				break;
			case 4:
			case 5:
				argumentType = ArgumentType.CLASSINFO;
				break;
			default:
				assert false : matchedPattern;
		}

		if (!scriptCommand && argumentType == ArgumentType.CLASSINFO) {
			Skript.error("Command event arguments are strings, meaning type specification is useless");
			return false;
		}

		//noinspection ConstantConditions - current structure will never be null
		List<Argument<?>> arguments = ((StructCommand) parser.getCurrentStructure()).getArguments();
		if (scriptCommand && (arguments == null || arguments.isEmpty())) {
			Skript.error("This command doesn't have any arguments");
			return false;
		}

		if (argumentType == ArgumentType.ORDINAL) {
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
			if (scriptCommand && ordinal > arguments.size()) { // Only check if it's a script command as we know nothing of command event arguments
				Skript.error("This command doesn't have a " + StringUtils.fancyOrderNumber(ordinal) + " argument");
				return false;
			}
		}

		if (scriptCommand) { // Handle before execution
			switch (argumentType) {
				case LAST:
					argument = arguments.get(arguments.size() - 1);
					break;
				case ORDINAL:
					argument = arguments.get(ordinal - 1);
					break;
				case SINGLE:
					if (arguments.size() == 1) {
						argument = arguments.get(0);
					} else {
						Skript.error("This command has multiple arguments, meaning it is not possible to get the 'argument'. Use 'argument 1', 'argument 2', etc. instead", ErrorQuality.SEMANTIC_ERROR);
						return false;
					}
					break;
				case ALL:
					Skript.error("'arguments' cannot be used for script commands. Use 'argument 1', 'argument 2', etc. instead", ErrorQuality.SEMANTIC_ERROR);
					return false;
				case CLASSINFO:
					ClassInfo<?> c = ((Literal<ClassInfo<?>>) expressions[0]).getSingle();
					if (!parseResult.regexes.isEmpty()) {
						ordinal = Utils.parseInt(parseResult.regexes.get(0).group());
						if (ordinal > arguments.size()) {
							Skript.error("This command doesn't have a " + StringUtils.fancyOrderNumber(ordinal) + " " + c + " argument", ErrorQuality.SEMANTIC_ERROR);
							return false;
						}
					}

					Argument<?> arg = null;
					int argAmount = 0;
					for (Argument<?> a : arguments) {
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
					assert false : argumentType;
					return false;
			}
		}

		return true;
	}
	
	@Override
	protected Object @Nullable [] get(Event event) {
		if (argument != null) { // handle this as a script command
			if (!(event instanceof ScriptCommandEvent)) {
				return new Object[0];
			}
			return argument.getValues((ScriptCommandEvent) event);
		}

		String fullCommand;
		if (event instanceof PlayerCommandPreprocessEvent) {
			fullCommand = ((PlayerCommandPreprocessEvent) event).getMessage().substring(1).trim();
		} else if (event instanceof ServerCommandEvent) { // It's a ServerCommandEvent then
			fullCommand = ((ServerCommandEvent) event).getCommand().trim();
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

		switch (argumentType) {
			case LAST:
				if (arguments.length > 0) {
					return new String[]{arguments[arguments.length - 1]};
				}
				break;
			case ORDINAL:
				if (arguments.length >= ordinal) {
					return new String[]{arguments[ordinal - 1]};
				}
				break;
			case SINGLE:
				if (arguments.length == 1) {
					return new String[]{arguments[arguments.length - 1]};
				}
				break;
			case ALL:
				return arguments;
		}

		return new Object[0];
	}

	@Override
	public boolean isSingle() {
		return argument != null ? argument.isSingle() : argumentType != ArgumentType.ALL;
	}
	
	@Override
	public Class<?> getReturnType() {
		return argument != null ? argument.getType() : String.class;
	}

	@Override
	public boolean isLoopOf(String typeString) {
		return typeString.equalsIgnoreCase("argument");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		switch (argumentType) {
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
