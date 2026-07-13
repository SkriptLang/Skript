package org.skriptlang.skript.bukkit.command.elements.expressions;

import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Command")
@Description("The command that caused an 'on command' event (excluding the leading slash and all arguments)")
@Example("""
	# prevent any commands except for the /exit command during some game
	on command:
		if {game::%player%::playing} is true:
			if the command is not "exit":
				message "You're not allowed to use commands during the game"
				cancel the event
	""")
@Since("2.0, 2.7 (support for script commands)")
@Events("command")
public class ExprCommand extends SimpleExpression<String> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprCommand.class, ExprCommand::new, String.class,
				"[the] (full|complete|whole) command",
				"[the] command [label|alias]"));
	}

	private boolean fullCommand;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		fullCommand = matchedPattern == 0;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerCommandPreprocessEvent.class, ServerCommandEvent.class, ScriptCommandExecutionEvent.class,
			CommandSuggestionEvent.class);
	}

	@Override
	protected String[] get(Event event) {
		String input = switch (event) {
			case PlayerCommandPreprocessEvent preprocessEvent -> preprocessEvent.getMessage().substring(1).trim();
			case ServerCommandEvent serverCommandEvent -> serverCommandEvent.getCommand().trim();
			case ScriptCommandExecutionEvent scriptCommandEvent -> scriptCommandEvent.getRawInput();
			case CommandSuggestionEvent suggestionEvent -> suggestionEvent.getFullInput();
			default -> throw new IllegalStateException("Unexpected value: " + event);
		};
		if (fullCommand) {
			return new String[]{input};
		} else {
			int space = input.indexOf(' ');
			return new String[] {space == -1 ? input : input.substring(0, space)};
		}
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
		return fullCommand ? "the full command" : "the command";
	}

}
