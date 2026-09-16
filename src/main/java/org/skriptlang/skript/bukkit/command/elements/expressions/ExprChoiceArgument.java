package org.skriptlang.skript.bukkit.command.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandEvent;
import org.skriptlang.skript.bukkit.command.elements.expressions.ExprArgument.ArgumentType;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;

@Name("Choice Argument")
@Description("""
	Represents the literal argument out of a choice group selected by the command sender.
	Choice arguments, just like regular arguments, are numbered in order of occurrence (left to right).
	This expression also supports optional literal arguments.
	""")
@Example("""
	command /alert (restart|giveaway):
		trigger:
			if the choice argument is "restart":
				broadcast "<red>The server is restarting in 5 minutes!"
			else: # must be giveaway
				broadcast "<aqua>A giveaway is starting soon at spawn!"
	""")
@Since("INSERT VERSION")
public class ExprChoiceArgument extends SimpleExpression<String> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.simple(
			ExprChoiceArgument.class, ExprChoiceArgument::new, String.class,
			"[the] choice arg[ument] <(\\d+)>", // ORDINAL
			"[the] <(\\d*1)st|(\\d*2)nd|(\\d*3)rd|(\\d*[4-90])th> choice arg[ument][s]", // ORDINAL
			"[the] [:last] choice arg[ument]" // SINGLE or LAST
		));
	}

	private ArgumentType type;
	private int ordinal = -1; // Available in ORDINAL
	private Set<String> choices;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		List<Set<String>> choices = getParser().getData(CommandParsingData.class).getChoices();
		if (choices.isEmpty()) {
			Skript.error("This command doesn't have any choice arguments");
			return false;
		}

		type = switch (matchedPattern) {
			case 0, 1 -> ArgumentType.ORDINAL;
			case 2 -> parseResult.hasTag("last") ? ArgumentType.LAST : ArgumentType.SINGLE;
			default -> throw new IllegalStateException("Matched pattern must be 0-2");
		};

		switch (type) {
			case ORDINAL -> {
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
				if (ordinal > choices.size()) {
					Skript.error("This command doesn't have a " + StringUtils.fancyOrderNumber(ordinal) + " choice argument");
					return false;
				}
				this.choices = choices.get(ordinal - 1);
			}
			case SINGLE -> {
				if (choices.size() != 1) {
					Skript.error("This command has multiple choice arguments, meaning it is not possible to get the 'choice argument'." +
						" Use 'choice argument 1', 'choice argument 2', etc. instead");
					return false;
				}
				this.choices = choices.getFirst();
			}
			case LAST -> this.choices = choices.getLast();
		}

		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{ScriptCommandEvent.class};
	}

	@Override
	protected String[] get(Event event) {
		// TODO an approach resilient to duplicate literals would be best
		// challenge: this is hard to do because of optionals that make the tree incredibly complex
		// for now, this works good enough!
		for (var node : ((ScriptCommandEvent) event).getContext().getNodes()) {
			if (!(node.getNode() instanceof LiteralCommandNode<?> literalNode)) {
				continue;
			}
			if (choices.contains(literalNode.getLiteral())) {
				return new String[]{literalNode.getLiteral()};
			}
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (type) {
			case ORDINAL -> "choice argument " + ordinal;
			case SINGLE -> "choice argument";
			case LAST -> "last choice argument";
			default -> throw new IllegalStateException("Unrecognized choice argument type");
		};
	}

}
