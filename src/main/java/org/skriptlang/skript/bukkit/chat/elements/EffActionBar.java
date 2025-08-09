package org.skriptlang.skript.bukkit.chat.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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

@Name("Action Bar")
@Description("Sends an action bar message to a player.")
@Examples("send action bar \"Hello player!\" to player")
@Since({
	"2.3",
	"INSERT VERSION (support for sending anything)"
})
public class EffActionBar extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffActionBar.class)
			.supplier(EffActionBar::new)
			.addPattern("send [the] action[ ]bar [with text] %object% [to %commandsenders%]")
			.build());
	}

	private Expression<?> message;
	private Expression<CommandSender> recipients;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		message = LiteralUtils.defendExpression(expressions[0]);
		recipients = LiteralUtils.defendExpression(expressions[1]);
		if (!LiteralUtils.canInitSafely(message, recipients)) {
			return false;
		}

		//noinspection unchecked
		var componentMessage = expressions[0].getConvertedExpression(Component.class);
		if (componentMessage != null) {
			message = componentMessage;
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		Object message = this.message.getSingle(event);
		Component component;
		if (message instanceof Component c) {
			component = c;
		} else {
			component = ChatComponentHandler.plain(message);
		}

		Audience.audience(recipients.getArray(event)).sendActionBar(component);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("send the action bar", message);
		if (recipients != null) {
			builder.append("to", recipients);
		}
		return builder.toString();
	}

}
