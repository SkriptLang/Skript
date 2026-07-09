package org.skriptlang.skript.bukkit.command.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.elements.expressions.ExprCommandSuggestions;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.ScriptSuggestionProvider.FilteringMode;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Script Command Suggestion Filtering Mode")
@Description("""
	Controls how suggestions are filtered for script commands. \
	With no filtering, suggestions are simply displayed as set. \
	With 'starts with' filtering, only suggestions that start with the current input are displayed. \
	With 'contains' filtering, only suggestions that contain the current input are displayed.
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
public class EffSuggestionFilteringMode extends Effect implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		String rest = " (for|with) %objects%";
		syntaxRegistry.register(SyntaxRegistry.EFFECT,
			SyntaxInfo.simple(EffSuggestionFilteringMode.class, EffSuggestionFilteringMode::new,
				"(use no|disable) filtering" + rest,
				"use starts with filtering" + rest,
				"use contains filtering" + rest));
	}

	private ExprCommandSuggestions suggestions;
	private FilteringMode filteringMode;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!(expressions[0] instanceof ExprCommandSuggestions exprCommandSuggestions)) {
			Skript.error("It is only possible to change the filtering mode of suggestions!");
			return false;
		}
		suggestions = exprCommandSuggestions;
		filteringMode = FilteringMode.values()[matchedPattern];
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{CommandSuggestionEvent.class};
	}

	@Override
	protected void execute(Event event) {
		CommandSuggestionEvent suggestionEvent = (CommandSuggestionEvent) event;
		if (suggestionEvent.getCurrentArgument() == suggestions.argument.argument) {
			suggestionEvent.filteringMode = filteringMode;
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (filteringMode) {
			case NONE -> "disable filtering";
			case STARTS_WITH -> "use starts with filtering";
			case CONTAINS -> "use contains filtering";
		} + " for " + suggestions.toString(event, debug);
	}

}
