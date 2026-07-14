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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent;
import org.skriptlang.skript.bukkit.command.elements.structures.util.CommandSuggestionEvent.CommandSuggestion;
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
@Example("""
	command /home2:
		subcommand set <name: text>:
			trigger:
				set {homes::%player%::%{_name}%} to the player's location
		subcommand <name: text>:
			suggestions:
				loop {homes::%player%::*}:
					add formatted "<ttp:'Location: %loop-value%'>%loop-index%" to the suggestions for the text argument
			trigger:
				teleport the player to {homes::%player%::%{_name}%}
	""")
@Since("INSERT VERSION")
public class ExprCommandSuggestions extends SimpleExpression<Component> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprCommandSuggestions.class, ExprCommandSuggestions::new, Component.class,
				"[the] [command] (suggestions|tab completions) (of|for) %objects%",
				"%objects%'[s] [command] (suggestions|tab completions)"));
	}

	@ApiStatus.Internal
	public ExprArgument argument;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!(expressions[0] instanceof ExprArgument exprArgument) || exprArgument.argument == null) {
			Skript.error("It is only possible to obtain or change command suggestions of an argument!" +
				" Is the argument expression written correctly?");
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
	protected Component[] get(Event event) {
		assert argument.argument != null;
		List<CommandSuggestion> suggestions = ((CommandSuggestionEvent) event).suggestions.get(argument.argument.name());
		if (suggestions == null) {
			return new Component[0];
		}
		return suggestions.stream()
			.map(suggestion -> {
				Component result = Component.text(suggestion.suggestion());
				if (suggestion.tooltip() != null) {
					result = result.hoverEvent(suggestion.tooltip());
				}
				return result;
			})
			.toArray(Component[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET ->
				new Class[]{String[].class, argument.getReturnType().arrayType(), Component[].class};
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

		List<CommandSuggestion> currentSuggestions = ((CommandSuggestionEvent) event).suggestions
			.computeIfAbsent(argumentName, ignored -> new ArrayList<>());

		switch (mode) {
			case SET:
				currentSuggestions.clear();
				//$FALL-THROUGH$
			case ADD:
				assert delta != null;
				for (Object object : delta) {
					currentSuggestions.add(asSuggestion(object));
				}
				break;
			case REMOVE:
				assert delta != null;
				for (Object object : delta) {
					currentSuggestions.remove(asSuggestion(object));
				}
				break;
			case DELETE:
				currentSuggestions.clear();
				break;
		}
	}

	private CommandSuggestion asSuggestion(Object object) {
		return switch (object) {
			case String string -> new CommandSuggestion(string);
			case Component component when !Component.class.isAssignableFrom(argument.getReturnType()) -> {
				Component tooltip = null;
				var hoverEvent = component.hoverEvent();
				if (hoverEvent != null && hoverEvent.value() instanceof Component hover) {
					tooltip = hover;
				}
				yield new CommandSuggestion(PlainTextComponentSerializer.plainText().serialize(component), tooltip);
			}
			default -> new CommandSuggestion(Classes.toString(object));
		};
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the command suggestions of " + argument.toString(event, debug);
	}

}
