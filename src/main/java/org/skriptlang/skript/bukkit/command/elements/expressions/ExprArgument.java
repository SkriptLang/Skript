package org.skriptlang.skript.bukkit.command.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandEvent;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.Array;
import java.util.List;
import java.util.regex.MatchResult;

@Name("Argument")
@Description("""
	A user provided value in a command execution.
	For example, if the command "/tell <player> <text>" is used like "/tell Njol Hello Njol!", 'argument 1' is the player named "Njol" and 'argument 2' is the text "Hello Njol!".
	One can also use the type of the argument instead of its index to address the argument.
	For example, in the command example above, 'player-argument' is the same as 'argument 1'.
	Usable in script commands and command events.
	Please note that specifying the argument type is only supported in script commands.
	""")
@Example("give the item-argument to the player-argument")
@Example("damage the player-argument by the number-argument")
@Example("give a diamond pickaxe to the argument")
@Example("add argument 1 to argument 2")
@Example("heal the last argument")
@Since("1.0, 2.7 (support for command events)")
public class ExprArgument extends SimpleExpression<Object> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.simple(
			ExprArgument.class, ExprArgument::new, Object.class,
			"[the] last arg[ument]", // LAST
			"[the] arg[ument](-| )<(\\d+)>", // ORDINAL
			"[the] <(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th> arg[ument][s]", // ORDINAL
			"[(all [[of] the]|the)] arg[ument][all:s]", // SINGLE OR ALL
			"[the] %*classinfo%( |-)arg[ument][( |-)<\\d+>]", // CLASSINFO
			"[the] arg[ument]( |-)%*classinfo%[( |-)<\\d+>]" // CLASSINFO
		));
	}

	private enum ArgumentType {

		LAST, ORDINAL, SINGLE, ALL, CLASSINFO

	}

	private ArgumentType type;
	private int ordinal = -1; // Available in ORDINAL and sometimes CLASSINFO

	@Nullable ArgumentData<?> argument;

	private boolean couldCauseArithmeticConfusion = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ParserInstance parser = getParser();
		boolean scriptCommand = parser.isCurrentEvent(ScriptCommandEvent.class, CommandSuggestionEvent.class);

		type = switch (matchedPattern) {
			case 0 -> ArgumentType.LAST;
			case 1, 2 -> ArgumentType.ORDINAL;
			case 3 -> {
				if (parseResult.hasTag("all")) {
					yield ArgumentType.ALL;
				}
				if (parseResult.expr.matches("(the )?arg(ument)?")) {
					couldCauseArithmeticConfusion = true; // 'arg-1' could be parsed as 'argument - 1'
				}
				yield ArgumentType.SINGLE;
			}
			case 4, 5 -> ArgumentType.CLASSINFO;
			default -> throw new IllegalArgumentException("Unexpected matched pattern: " + matchedPattern);
		};

		if (!scriptCommand && type == ArgumentType.CLASSINFO) {
			Skript.error("Command event arguments are strings, meaning type specification is useless");
			return false;
		}

		List<ArgumentData<?>> currentArguments = null;
		if (scriptCommand) {
			currentArguments = parser.getData(CommandParsingData.class).getArguments();
			if (currentArguments.isEmpty()) {
				Skript.error("This command doesn't have any arguments");
				return false;
			}
		}

		if (type == ArgumentType.ORDINAL) {
			// Figure out in which format (1st, 2nd, 3rd, Nth) argument was given in
			MatchResult regex = parseResult.regexes.getFirst();
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
			switch (type) {
				case LAST -> argument = currentArguments.getLast();
				case ORDINAL -> argument = currentArguments.get(ordinal - 1);
				case SINGLE -> {
					if (currentArguments.size() == 1) {
						argument = currentArguments.getFirst();
					} else {
						Skript.error("This command has multiple arguments, meaning it is not possible to get the 'argument'. Use 'argument 1', 'argument 2', etc. instead");
						return false;
					}
				}
				case ALL -> Skript.error("'arguments' cannot be used for script commands. Use 'argument 1', 'argument 2', etc. instead");
				case CLASSINFO -> {
					//noinspection unchecked
					ClassInfo<?> info = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
					if (!parseResult.regexes.isEmpty()) {
						ordinal = Utils.parseInt(parseResult.regexes.getFirst().group());
						if (ordinal > currentArguments.size()) {
							Skript.error("This command doesn't have a " + StringUtils.fancyOrderNumber(ordinal) + " " + info + " argument");
							return false;
						}
					}

					ArgumentData<?> arg = null;
					int argAmount = 0;
					for (ArgumentData<?> a : currentArguments) {
						if (!info.getC().isAssignableFrom(a.type().getC())) // This argument is not of the required type
							continue;

						if (ordinal == -1 && argAmount == 2) { // The user said '<type> argument' without specifying which, and multiple arguments for the type exist
							Skript.error("There are multiple " + type + " arguments in this command", ErrorQuality.SEMANTIC_ERROR);
							return false;
						}

						arg = a;

						argAmount++;
						if (argAmount == ordinal) { // There is argNum argument for the required type (ex: "string argument 2" would exist)
							break;
						}
					}

					if (argAmount == 0) {
						Skript.error("There is no " + type + " argument in this command");
						return false;
					} else if (ordinal > argAmount) { // The user wanted an argument number that didn't exist for the given type
						if (argAmount == 1) {
							Skript.error("There is only one " + type + " argument in this command");
						} else {
							Skript.error("There are only " + argAmount + " " + type + " arguments in this command");
						}
						return false;
					}

					// 'arg' will never be null here
					argument = arg;
				}
			}
		}

		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		// important note: this expression is not actually evaluated for CommandSuggestionEvent
		return CollectionUtils.array(ScriptCommandEvent.class, PlayerCommandPreprocessEvent.class, ServerCommandEvent.class,
			CommandSuggestionEvent.class);
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		if (event instanceof CommandSuggestionEvent) {
			error("Arguments cannot be obtained before the command is executed!");
			assert argument != null;
			return (Object[]) Array.newInstance(argument.type().getC(), 0);
		}

		if (argument != null) {
			Object value = ((ScriptCommandEvent) event).getArgument(argument.name());
			if (argument.isSingle()) {
				Object[] result = (Object[]) Array.newInstance(argument.type().getC(), 1);
				result[0] = value;
				return result;
			} else {
				return (Object[]) value;
			}
		}

		String fullCommand;
		if (event instanceof PlayerCommandPreprocessEvent preprocessEvent) {
			fullCommand = preprocessEvent.getMessage().substring(1).trim();
		} else if (event instanceof ServerCommandEvent serverCommandEvent) {
			fullCommand = serverCommandEvent.getCommand().trim();
		} else {
			return new String[0];
		}

		String[] arguments;
		int firstSpace = fullCommand.indexOf(' ');
		if (firstSpace != -1) {
			fullCommand = fullCommand.substring(firstSpace + 1);
			arguments = fullCommand.split(" ");
		} else { // No arguments, just the command
			return new String[0];
		}

		return switch (type) {
			case LAST -> {
				if (arguments.length > 0) {
					yield new String[]{arguments[arguments.length - 1]};
				}
				yield new String[0];
			}
			case ORDINAL -> {
				if (arguments.length >= ordinal) {
					yield new String[]{arguments[ordinal - 1]};
				}
				yield new String[0];
			}
			case SINGLE -> {
				if (arguments.length == 1) {
					yield new String[]{arguments[arguments.length - 1]};
				}
				yield new String[0];
			}
			case ALL -> arguments;
			default -> new String[0];
		};
	}

	@Override
	public boolean isSingle() {
		return argument == null ? type != ArgumentType.ALL : argument.isSingle();
	}

	@Override
	public Class<?> getReturnType() {
		return argument == null ? String.class : argument.type().getC();
	}

	@Override
	public boolean isLoopOf(String input) {
		return input.equalsIgnoreCase("argument");
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (type) {
			case LAST -> "the last argument";
			case ORDINAL -> "the " + StringUtils.fancyOrderNumber(ordinal) + " argument";
			case SINGLE -> "the argument";
			case ALL -> "the arguments";
			case CLASSINFO -> {
				assert argument != null;
				yield "the " + argument.type() + " argument " + (ordinal != -1 ? ordinal : "");
			}
		};
	}

	/**
	 * @return whether the expression is 'arg', a single argument that could cause confusion with 'arg-1' being parsed as 'argument - 1'.
	 */
	public boolean couldCauseArithmeticConfusion() {
		return couldCauseArithmeticConfusion;
	}

}
