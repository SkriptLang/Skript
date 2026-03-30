package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Events;
import ch.njol.skript.effects.EffChange;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Chat Format")
@Description({
	"Can be used to modify the chat format.",
	"The sender of a message is represented by [player] or [sender].",
	"The message is represented by [message] or [msg].",
})
@Example("set the chat format to \"<yellow>[player]<light gray>: <green>[message]\"")
@Since("2.2-dev31")
@Events("chat")
public class ExprChatFormat extends SimpleExpression<Component> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprChatFormat.class, Component.class)
			.supplier(ExprChatFormat::new)
			.addPattern("[the] (message|chat) format[ting]")
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		var stackIterator = getParser().getParsingStack().iterator();
		if (!stackIterator.hasNext() || stackIterator.next().getSyntaxElementClass() != EffChange.class) {
			Skript.error("'" + toString(null, false) + "' can only be changed, not obtained");
			return false;
		}
		return true;
	}

	@Override
	protected Component[] get(Event event) {
		error("'" + toString(null, false) + "' cannot be obtained. Returning <none>.");
		return new Component[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("'" + toString(null, false) + "' can't be changed after the event has passed");
			return null;
		}
		return switch (mode) {
			case SET, RESET -> new Class[]{Component.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof AsyncChatEvent asyncChatEvent)) {
			return;
		}

		if (delta == null) {
			// this is a replica implementation of ChatRenderer.defaultRenderer()
			// on some Paper versions, the default renderer is not handled properly, leading to unexpected formatting
			asyncChatEvent.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) ->
				Component.translatable("chat.type.text", sourceDisplayName, message)));
			return;
		}

		asyncChatEvent.renderer(ChatRenderer.viewerUnaware(((source, sourceDisplayName, message) ->
			((Component) delta[0]).replaceText(TextReplacementConfig.builder()
				.match("(?i)\\[(player|sender|message|msg)]")
				.replacement(((matchResult, builder) ->
					matchResult.group(1).startsWith("m") ? message : sourceDisplayName))
				.build()))));
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
		return "the chat format";
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{AsyncChatEvent.class};
	}

}
