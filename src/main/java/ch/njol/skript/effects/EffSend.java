package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.ExprColoured;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.common.AnyReceiver;
import ch.njol.skript.lang.util.common.AnySender;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Name("Message")
@Description({"Sends a message to the given player. Only styles written",
		"in given string or in <a href=expressions.html#ExprColored>formatted expressions</a> will be parsed.",
		"Adding an optional sender allows the messages to be sent as if a specific player sent them.",
		"This is useful with Minecraft 1.16.4's new chat ignore system, in which players can choose to ignore other players,",
		"but for this to work, the message needs to be sent from a player."})
@Examples({"message \"A wild %player% appeared!\"",
		"message \"This message is a distraction. Mwahaha!\"",
		"send \"Your kill streak is %{kill streak::%uuid of player%}%.\" to player",
		"if the targeted entity exists:",
		"\tmessage \"You're currently looking at a %type of the targeted entity%!\"",
		"on chat:",
		"\tcancel event",
		"\tsend \"[%player%] >> %message%\" to all players from player"})
@RequiredPlugins("Minecraft 1.16.4+ for optional sender")
@Since("1.0, 2.2-dev26 (advanced features), 2.5.2 (optional sender), 2.6 (sending objects)")
public class EffSend extends Effect {

	static {
		Skript.registerEffect(EffSend.class,
			"(message|send [message[s]]) %objects% [to %receivers%] [from %-senders%]"
		);
	}

	private Expression<?>[] messages;

	/**
	 * Used for {@link EffSend#toString(Event, boolean)}
	 */
	private Expression<?> messageExpr;

	private Expression<AnyReceiver<?, ?>> recipients;

	private @Nullable Expression<AnySender<?>> sender;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		messageExpr = LiteralUtils.defendExpression(exprs[0]);

		messages = messageExpr instanceof ExpressionList<?> list
			? list.getExpressions()
			: new Expression[] {messageExpr};
		recipients = (Expression<AnyReceiver<?, ?>>) exprs[1];
		if (exprs.length > 2)
			sender = (Expression<AnySender<?>>) exprs[2];
		return LiteralUtils.canInitSafely(messageExpr);
	}

	@Override
	protected void execute(Event event) {
		@Nullable AnySender<?> sender = this.sender != null ? this.sender.getSingle(event) : null;
		for (Expression<?> message : this.getMessages()) {
			final Object @NotNull [] toSend;

			if (message instanceof VariableString string) {
				toSend = new Object[] {BungeeConverter.convert(string.getMessageComponents(event))};
			} else if (message instanceof ExprColoured && ((ExprColoured) message).isUnsafeFormat()) {
				toSend = Arrays.stream(message.getArray(event))
					.map(object -> BungeeConverter.convert(ChatMessages.parse((String) object)))
					.toArray(Object[]::new);
			} else {
				toSend = message.getArray(event);
			}

			for (Object send : toSend) {
				for (AnyReceiver<?, ?> recipient : this.recipients.getArray(event)) {
					recipient.sendSafely(send, sender);
				}
			}
		}
	}

	private Expression<?>[] getMessages() {
		if (messageExpr instanceof ExpressionList && !messageExpr.getAnd()) {
			return new Expression[] {CollectionUtils.getRandom(messages)};
		}
		return messages;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "send " + messageExpr.toString(event, debug) + " to " + recipients.toString(event, debug) +
			(sender != null ? " from " + sender.toString(event, debug) : "");
	}

}
