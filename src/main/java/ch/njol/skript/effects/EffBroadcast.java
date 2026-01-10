package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprColoured;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.skript.util.chat.MessageComponent;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

@Name("Broadcast")
@Description("Broadcasts a message to the server.")
@Example("broadcast \"Welcome %player% to the server!\"")
@Example("broadcast \"Woah! It's a message!\"")
@Since("1.0, 2.6 (broadcasting objects), 2.6.1 (using advanced formatting)")
public class EffBroadcast extends Effect {

	private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&x((?:&\\p{XDigit}){6})");

	static {
		Skript.registerEffect(EffBroadcast.class, "broadcast %objects% [(to|in) %-worlds%]");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?> messageExpr;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<?>[] messages;
	@Nullable
	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		messageExpr = LiteralUtils.defendExpression(exprs[0]);
		messages = messageExpr instanceof ExpressionList ?
			((ExpressionList<?>) messageExpr).getExpressions() : new Expression[] {messageExpr};
		worlds = (Expression<World>) exprs[1];
		return LiteralUtils.canInitSafely(messageExpr);
	}

	/**
	 * This effect will call {@link BroadcastMessageEvent} as of 2.9.0.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void execute(Event event) {
		List<CommandSender> receivers = new ArrayList<>();
		if (worlds == null) {
			receivers.addAll(Bukkit.getOnlinePlayers());
			receivers.add(Bukkit.getConsoleSender());
		} else {
			for (World world : worlds.getArray(event))
				receivers.addAll(world.getPlayers());
		}

		for (Expression<?> message : getMessages()) {
			if (message instanceof VariableString variableString) {
				// get both unformatted and components with single evaluation: https://github.com/SkriptLang/Skript/issues/7718
				StringBuilder unformattedString = new StringBuilder();
				List<MessageComponent> messageComponents = variableString.getMessageComponents(event, unformattedString);
				if (!dispatchEvent(unformattedString.toString(), receivers))
					continue;
				BaseComponent[] components = BungeeConverter.convert(messageComponents);
				receivers.forEach(receiver -> receiver.spigot().sendMessage(components));
			} else if (message instanceof ExprColoured coloured && coloured.isUnsafeFormat()) { // Manually marked as trusted
				for (Object realMessage : message.getArray(event)) {
					if (!dispatchEvent(Utils.replaceChatStyles((String) realMessage), receivers))
						continue;
					BaseComponent[] components = BungeeConverter.convert(ChatMessages.parse((String) realMessage));
					receivers.forEach(receiver -> receiver.spigot().sendMessage(components));
				}
			} else {
				for (Object messageObject : message.getArray(event)) {
					String realMessage = messageObject instanceof String string ? string : Classes.toString(messageObject);
					if (!dispatchEvent(Utils.replaceChatStyles(realMessage), receivers))
						continue;
					receivers.forEach(receiver -> receiver.sendMessage(realMessage));
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "broadcast " + messageExpr.toString(event, debug) + (worlds == null ? "" : " to " + worlds.toString(event, debug));
	}

	private Expression<?>[] getMessages() {
		if (messageExpr instanceof ExpressionList && !messageExpr.getAnd()) {
			return new Expression[] {CollectionUtils.getRandom(messages)};
		}
		return messages;
	}

	/**
	 * Manually calls a {@link BroadcastMessageEvent}.
	 * @param message the message
	 * @return true if the dispatched event does not get cancelled
	 */
	@SuppressWarnings("removal")
	private static boolean dispatchEvent(String message, List<CommandSender> receivers) {
		Set<CommandSender> recipients = Set.copyOf(receivers);
		BroadcastMessageEvent broadcastEvent;
		if (Skript.isRunningMinecraft(1, 14)) {
			broadcastEvent = new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients);
		} else {
			broadcastEvent = new BroadcastMessageEvent(message, recipients);
		}
		Bukkit.getPluginManager().callEvent(broadcastEvent);
		return !broadcastEvent.isCancelled();
	}

	/**
	 * Gets the raw string from the expression, replacing colour codes.
	 * @param event the event
	 * @param string the expression
	 * @return the raw string
	 */
	private static @Nullable String getRawString(Event event, Expression<? extends String> string) {
		if (string instanceof VariableString variableString)
			return variableString.toUnformattedString(event);
		String rawString = string.getSingle(event);
		if (rawString == null)
			return null;
		rawString = SkriptColor.replaceColorChar(rawString);
		if (rawString.toLowerCase().contains("&x")) {
			rawString = StringUtils.replaceAll(rawString, HEX_PATTERN, matchResult ->
				"<#" + matchResult.group(1).replace("&", "") + '>');
		}
		return rawString;
	}

}
