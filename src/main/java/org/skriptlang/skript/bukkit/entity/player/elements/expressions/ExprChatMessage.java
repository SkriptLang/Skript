package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

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
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Chat Message")
@Description("The chat message in a chat event.")
@Example("""
	on chat:
		player has permission "admin"
		set the message to "<light red>%message%"
	""")
@Since("1.4.6, 2.15 (support for reset)")
@Events("chat")
public class ExprChatMessage extends SimpleExpression<Component> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprChatMessage.class, Component.class)
			.supplier(ExprChatMessage::new)
			.addPattern("[the] [chat( |-)]message")
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Component[] get(Event event) {
		if (event instanceof AsyncChatEvent asyncChatEvent) {
			return new Component[]{asyncChatEvent.message()};
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
			case SET, DELETE, RESET -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof AsyncChatEvent asyncChatEvent)) {
			return;
		}

		asyncChatEvent.message(switch (mode) {
			case SET -> {
				assert delta != null;
				yield (Component) delta[0];
			}
			case DELETE -> Component.empty();
			case RESET -> asyncChatEvent.originalMessage();
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
		return "the chat message";
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{AsyncChatEvent.class};
	}

}
