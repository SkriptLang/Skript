package org.skriptlang.skript.bukkit.command.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Script Command Suggestions")
@Description("""
	Usable in a script command's "suggestions" entry.
	Allows modifying the suggestions displayed while a command is being typed.
	""")
@Example("""
	command /menu <text> <text>:
	    suggestions:
	        if arg-1 is not set:
	            set arg-1's suggestions to "Appetizers", "Entrees", and "Desserts"
	        else if arg-1 is "Appetizers":
	            set arg-2's suggestions to "Salads", "Breads", and "Fried Delights"
	        else if arg-1 is "Entrees":
	            set arg-2's suggestions to "Pastas", "Handhelds", and "Pizzas"
	        else if arg-1 is "Desserts":
	            set arg-2's suggestions to "Cakes", "Ice Creams", and "Pies"
	    trigger:
	        send "Yum!"
	""")
@Since("INSERT VERSION")
public class ExprCommandSuggestions extends SimpleExpression<String> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprCommandSuggestions.class, ExprCommandSuggestions::new, String.class,
				"[the] [command] (suggestions|tab completions) (of|for) %objects%",
				"%objects%'[s] [command] (suggestions|tab completions)"));
	}

	@ApiStatus.Internal
	public ExprArgument argument;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!(expressions[0] instanceof ExprArgument exprArgument) || exprArgument.argument == null) {
			Skript.error("It is only possible to obtain or change command suggestions of an argument!");
			return false;
		}
		argument = exprArgument;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{CommandSuggestionEvent.class};
	}

	@Override
	protected String @Nullable [] get(Event event) {
		assert argument.argument != null;
		List<String> suggestions = ((CommandSuggestionEvent) event).suggestions.get(argument.argument.name());
		return suggestions == null ? new String[0] : suggestions.toArray(new String[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET -> new Class[]{String[].class, argument.getReturnType().arrayType()};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert argument.argument != null;
		String argumentName = argument.argument.name();

		if (mode == ChangeMode.RESET) {
			((CommandSuggestionEvent) event).suggestions.remove(argumentName);
			return;
		}

		List<String> currentSuggestions = ((CommandSuggestionEvent) event).suggestions
			.computeIfAbsent(argumentName, ignored -> new ArrayList<>());

		switch (mode) {
			case SET:
				currentSuggestions.clear();
				//$FALL-THROUGH$
			case ADD:
				assert delta != null;
				for (Object object : delta) {
					if (object instanceof String string) {
						currentSuggestions.add(string);
					} else {
						currentSuggestions.add(Classes.toString(object));
					}
				}
				break;
			case REMOVE:
				assert delta != null;
				for (Object object : delta) {
					if (object instanceof String string) {
						currentSuggestions.remove(string);
					} else {
						currentSuggestions.remove(Classes.toString(object));
					}
				}
				break;
			case DELETE:
				currentSuggestions.clear();
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the command suggestions of " + argument.toString(event, debug);
	}

}
