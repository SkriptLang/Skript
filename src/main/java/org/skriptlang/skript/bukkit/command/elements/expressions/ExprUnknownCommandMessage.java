package org.skriptlang.skript.bukkit.command.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.event.command.UnknownCommandEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Unknown Command Message")
@Description("The message sent to a player when executing an unknown command.")
@Example("""
	on unknown command execution:
		add 1 to {-command_fails::%player%}
		wait 30 seconds:
			remove 1 from {-command_fails::%player%}
		if {-command_fails::%player%} is greater than or equal to 5:
			push the player upwards
			set the unknown command message to "<red>You are executing too many unknown commands too fast!"
""")
@Since("INSERT VERSION")
@Events("unknown command execution")
public class ExprUnknownCommandMessage extends SimpleExpression<Component> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.simple(ExprUnknownCommandMessage.class, ExprUnknownCommandMessage::new, Component.class,
				"[the] (unknown|non-existent) command message"));
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{UnknownCommandEvent.class};
	}

	@Override
	protected Component[] get(Event event) {
		if (event instanceof UnknownCommandEvent unknownCommandEvent) {
			return new Component[]{unknownCommandEvent.message()};
		}
		return new Component[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("'" + toString(null, false) + "' can't be changed after the event has passed");
			return null;
		}
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof UnknownCommandEvent unknownCommandEvent)) {
			return;
		}

		unknownCommandEvent.message(switch (mode) {
			case SET -> {
				assert delta != null;
				yield (Component) delta[0];
			}
			case DELETE -> null;
			default -> throw new IllegalStateException("Unexpected change mode: " + mode);
		});
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the unknown command message";
	}

}
