package org.skriptlang.skript.bukkit.chat.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.chat.ChatComponentHandler;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Set;

@Name("Broadcast")
@Description("Broadcasts a message to the server.")
@Example("broadcast \"Welcome %player% to the server!\"")
@Example("broadcast \"Woah! It's a message!\"")
@Since({
	"1.0",
	"2.6 (support for broadcasting anything)",
	"2.6.1 (using advanced formatting)"
})
public class EffBroadcast extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffBroadcast.class)
			.supplier(EffBroadcast::new)
			.addPattern("broadcast %objects% [(to|in) %-worlds%]")
			.build());
	}

	private Expression<?> messages;
	private @Nullable Expression<World> worlds;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		messages = LiteralUtils.defendExpression(expressions[0]);
		if (!LiteralUtils.canInitSafely(messages)) {
			return false;
		}
		//noinspection unchecked
		var componentMessages = expressions[0].getConvertedExpression(Component.class);
		if (componentMessages != null) {
			messages = componentMessages;
		}

		if (expressions[1] != null) {
			worlds = LiteralUtils.defendExpression(expressions[1]);
			return LiteralUtils.canInitSafely(worlds);
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

		// determine recipients
		ImmutableSet.Builder<CommandSender> recipientsBuilder = ImmutableSet.builder();
		if (worlds == null) {
			recipientsBuilder.addAll(Bukkit.getOnlinePlayers());
			recipientsBuilder.add(Bukkit.getConsoleSender());
		} else {
			for (World world : worlds.getArray(event)) {
				recipientsBuilder.addAll(world.getPlayers());
			}
		}
		Set<CommandSender> recipients = recipientsBuilder.build();

		Audience audience = Audience.audience(recipients);
		boolean isAsync = !Bukkit.isPrimaryThread();
		for (Component component : components) {
			if (new BroadcastMessageEvent(isAsync, component, recipients).callEvent()) {
				audience.sendMessage(component);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("broadcast", messages);
		if (worlds != null) {
			builder.append("in", worlds);
		}
		return builder.toString();
	}

}
