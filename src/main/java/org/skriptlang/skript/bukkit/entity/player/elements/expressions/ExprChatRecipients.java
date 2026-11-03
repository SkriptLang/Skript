package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Events;
import ch.njol.skript.lang.EventRestrictedSyntax;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Chat Recipients")
@Description("The recipients of a chat event")
@Example("chat recipients")
@Since("2.2-Fixes-v7, 2.2-dev35 (clearing recipients), 2.15 (returns Audience)")
@Events("chat")
public class ExprChatRecipients extends SimpleExpression<Audience> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprChatRecipients.class, Audience.class)
			.supplier(ExprChatRecipients::new)
			.addPattern("[the] [chat( | -)]recipients")
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Audience[] get(Event event) {
		if (event instanceof AsyncChatEvent asyncChatEvent) {
			return asyncChatEvent.viewers().toArray(new Audience[0]);
		}
		return new Audience[0];
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("'" + toString(null, false) + "' can't be changed after the event has passed");
			return null;
		}
		return switch (mode) {
			case ADD, SET, REMOVE, DELETE, RESET -> CollectionUtils.array(Audience[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof AsyncChatEvent asyncChatEvent)) {
			return;
		}

		switch (mode) {
			case ADD -> {
				assert delta != null;
				for (Object audience : delta) {
					asyncChatEvent.viewers().add((Audience) audience);
				}
			}
			case SET -> {
				assert delta != null;
				asyncChatEvent.viewers().clear();
				for (Object audience : delta) {
					asyncChatEvent.viewers().add((Audience) audience);
				}
			}
			case REMOVE -> {
				assert delta != null;
				for (Object audience : delta) {
					asyncChatEvent.viewers().remove((Audience) audience);
				}
			}
			case DELETE -> asyncChatEvent.viewers().clear();
			case RESET -> {
				asyncChatEvent.viewers().clear();
				asyncChatEvent.viewers().addAll(Bukkit.getOnlinePlayers());
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Audience> getReturnType() {
		return Audience.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the chat recipients";
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		//noinspection unchecked
		return new Class[]{AsyncChatEvent.class};
	}

}
