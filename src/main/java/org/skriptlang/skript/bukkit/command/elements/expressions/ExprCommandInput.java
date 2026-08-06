package org.skriptlang.skript.bukkit.command.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Script Command Suggestion Input")
@Description("""
	Usable in a script command's "suggestions" entry.
	This provides information including the full content of what the player has typed so far, \
	what they have typed as part of the current argument, \
	and at what position within the full content the current argument starts (starting from 1).
	""")
@Example("""
	command /message <player> <text>:
		suggestions:
			player argument is set # they are writing the text argument
			if the current input contains "frick":
				set the suggestions for the text argument to "Keep your messages nice please!"
				disable filtering for the text argument's suggestions
		trigger:
			send "<grey><italic>%player% -> You: %text argument%" to player argument
			send "<grey><italic>You -> %player argument%: %text argument%" to player
	""")
@Since("INSERT VERSION")
public class ExprCommandInput extends SimpleExpression<Object> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprCommandInput.class, ExprCommandInput::new, Object.class,
				"[the] [command] [suggestion|tab completion] (full|complete) input",
				"[the] [command] [suggestion|tab completion] (current|arg[ument]) input",
				"[the] [command] [suggestion|tab completion] (current|arg[ument]) input [start[ing]] (position|index)"));
	}

	private enum Type {
		FULL_INPUT,
		INPUT,
		INDEX
	}

	private Type type;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = Type.values()[matchedPattern];
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{CommandSuggestionEvent.class};
	}

	@Override
	protected Object[] get(Event event) {
		CommandSuggestionEvent suggestionEvent = (CommandSuggestionEvent) event;
		return switch (type) {
			case FULL_INPUT -> new String[]{suggestionEvent.getFullInput()};
			case INPUT -> new String[]{suggestionEvent.getInput()};
			case INDEX -> new Integer[]{suggestionEvent.getInputStartIndex() + 1};
		};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return type == Type.INDEX ? Integer.class : String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the command suggestion " + switch (type) {
			case FULL_INPUT -> "full input";
			case INPUT -> "input";
			case INDEX -> "input starting position";
		};
	}

}
