package org.skriptlang.skript.bukkit.chat.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.chat.ChatComponentHandler;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Message")
@Description({
	"Sends a message to a player (or other thing capable of receiving a message, such as the console)",
	"Only styles written in given string or in <a href=#ExprColored>formatted expressions</a> will be parsed.",
})
@Example("message \"A wild %player% appeared!\"")
@Example("message \"This message is a distraction. Mwahaha!\"")
@Example("send \"Your kill streak is %{kill streak::%uuid of player%}%.\" to player")
@Example("""
	if the targeted entity exists:
		message "You're currently looking at a %type of the targeted entity%!"
	""")
@Since({
	"1.0",
	"2.2-dev26 (advanced features)",
	"2.6 (support for sending anything)"
})
public class EffMessage extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffMessage.class)
			.supplier(EffMessage::new)
			.addPattern("(message|send [message[s]]) %objects% [to %commandsenders%]")
			.build());
	}

	private Expression<?> messages;
	private Expression<CommandSender> recipients;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		messages = LiteralUtils.defendExpression(expressions[0]);
		recipients = LiteralUtils.defendExpression(expressions[1]);
		if (!LiteralUtils.canInitSafely(messages, recipients)) {
			return false;
		}

		//noinspection unchecked
		var componentMessages = expressions[0].getConvertedExpression(Component.class);
		if (componentMessages != null) {
			messages = componentMessages;
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		Object[] messages = this.messages.getArray(event);
		Component[] components = new Component[messages.length];
		for (int i = 0; i < messages.length; i++) {
			if (messages[i] instanceof Component) {
				components[i] = (Component) messages[i];
			} else {
				components[i] = ChatComponentHandler.plain(messages[i]);
			}
		}

		Audience audience = Audience.audience(recipients.getArray(event));
		for (Component component : components) {
			audience.sendMessage(component);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("message", messages);
		if (recipients != null) {
			builder.append("to", recipients);
		}
		return builder.toString();
	}

}
